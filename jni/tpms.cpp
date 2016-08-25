// 包含头文件
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <pthread.h>
#include <termios.h>
#include <fcntl.h>
#include "tpms.h"

// 预编译开关
#define DUMP_DATA_RECV  1
#define DUMP_DATA_SEND  1

// 内部常量定义
#define TXRX_BUF_LEN    128

// 内部类型定义
typedef struct {
    int        fd;
    int        tires_total;
    int        tires_current;
    TPMS_TIRE  tires [MAX_TIRES_NUM];
    TPMS_ALERT alerts[MAX_ALERT_NUM];
    pthread_t  thread_tpms;

    #define TPMS_THREAD_EXIT (1 << 0)
    int        state;

    #define TPMS_HANDSHAKE_ACK (1 << 0)
    #define TPMS_ALERT_ACK     (1 << 1)
    #define TPMS_TIRES_ACK     (1 << 2)
    #define TPMS_UNWATCH_ACK   (1 << 3)
    #define TPMS_LEARN_ACK     (1 << 4)
    int        ack_flags;
    char       frame_data[TXRX_BUF_LEN];

    PFN_TPMS_CB callback;

#ifdef ENABLE_TPMS_JNI
    jclass     jcls_tpms;
    jobject    jobj_tpms;
    jmethodID  jmid_callback;
#endif
} TPMS_CONTEXT;

// 内部函数实现
#define TPMS_START_BYTE   0xaa
#define TPMS_MASTER_ADDR  0xa1
#define TPMS_SLAVE_ADDR   0x41
static int make_tpms_frame(char *frame, int mfn, int sfn, char *data, int dlen)
{
    int i;

    if (mfn == 0x11) {
        frame[0] = 0xaa;
        frame[1] = 0x41;
        frame[2] = 0xa1;
        frame[3] = 0x06;
        frame[4] = 0x11;
        frame[5] = 0xa3;
    }
    else {
        frame[0] = TPMS_START_BYTE;
        frame[1] = TPMS_SLAVE_ADDR;
        frame[2] = TPMS_MASTER_ADDR;
        frame[3] = 7 + dlen;
        frame[4] = mfn;
        frame[5] = sfn;
        frame[6 + dlen] = 0;
        memcpy(frame + 6, data, dlen);
        for (i=0; i<6+dlen; i++) {
            frame[6 + dlen] += frame[i];
        }
    }

#if DUMP_DATA_SEND
    printf("data send: ");
    for (i=0; i<frame[3]; i++) {
        printf("%02X ", frame[i]);
    }
    printf("\n");
#endif

    return frame[3];
}

static void* tpms_data_thread(void* arg)
{
    TPMS_CONTEXT *context = (TPMS_CONTEXT*)arg;
    int           offset  = 0;
    int           availn  = 0;
    int           i, m, n;

    //++ for select
    fd_set        fds;
    struct timeval tv;
    //-- for select

#ifdef ENABLE_TPMS_JNI
    JNIEnv *env = get_jni_env();
#endif

    while (1) {
        if (context->state & TPMS_THREAD_EXIT) {
            break;
        }

        FD_ZERO(&fds);
        FD_SET (context->fd, &fds);
        tv.tv_sec  = 1;
        tv.tv_usec = 0;
        if (select(context->fd + 1, &fds, NULL, NULL, &tv) <= 0) {
//          printf("select error or timeout !\n");
            continue;
        }

        availn = read(context->fd, context->frame_data, sizeof(context->frame_data));
#if DUMP_DATA_RECV
        if (availn > 0) {
            printf("data recv: ");
            for (i=0; i<availn; i++) {
                printf("%02X ", context->frame_data[i]);
            }
            printf("\n");
        }
#endif
        if (availn > 0) {
            char  header[] = { 0xaa, 0xa1, 0x41 };
            char *find     = context->frame_data;
            char  length;
            char  checksum;
            char  mfn;
            char  sfn;

            while (1) {
                find   = (char*)memmem(find, availn, header, sizeof(header));
                offset = find ? find - context->frame_data : -1;
                if (offset < 0) {
//                  printf("offset out of buffer !\n");
                    break;
                }

                length = context->frame_data[offset + 3];
                if (length < 6 || length + offset > availn) {
                    printf("length invalid !\n");
                    availn -= 4;
                    find   += 4;
                    continue;
                }

                checksum = 0;
                for (i=0; i<length-1; i++) {
                    checksum += context->frame_data[offset + i];
                }
                if (checksum != context->frame_data[offset + length - 1]) {
                    printf("checksum invalid !\n");
                    availn -= 4;
                    find   += 4;
                    continue;
                }

                // mfn & sfn
                offset += 4; mfn = offset < availn ? context->frame_data[offset] : 0;
                offset += 1; sfn = offset < availn ? context->frame_data[offset] : 0;

                switch (mfn) {
                case 0x11:
                    context->ack_flags &= ~TPMS_HANDSHAKE_ACK;
                    break;
                case 0x62:
                    m = (sfn == 0) ? 0 : sfn - 1;
                    n = (sfn == 0) ? MAX_ALERT_NUM : sfn;
                    for (i=m; i<n; i++) {
                        if (i != MAX_ALERT_NUM - 1) {
                            offset++; context->alerts[i].pressure_hot = offset < availn ? context->frame_data[offset] : 0;
                            offset++; context->alerts[i].pressure_low = offset < availn ? context->frame_data[offset] : 0;
                        }
                        else {
                            offset++; context->alerts[i].pressure_hot = offset < availn ? context->frame_data[offset] : 0;
                        }
                    }
                    context->ack_flags &= ~TPMS_ALERT_ACK;
                    if (context->callback) {
                        context->callback(context, mfn, sfn);
                    }
#ifdef ENABLE_TPMS_JNI
                    if (env && context->jcls_tpms && context->jobj_tpms && context->jmid_callback) {
                        env->CallVoidMethod(context->jobj_tpms, context->jmid_callback, mfn, sfn);
                    }
#endif
                    break;
                case 0x63:
                case 0x66:
                    if (sfn == 0 && /*context->tires_total == 0*/ length == 8) {
                        offset++;
                        context->tires_total   = offset < availn ? context->frame_data[offset] : 0;
                        context->tires_current = 0;
                        memset(context->tires, 0, sizeof(context->tires));
                    }
                    else {
                        n = (sfn == 0) ? context->tires_current : sfn - 1;
                        offset++; context->tires[n].sensor_id  = (offset < availn ? context->frame_data[offset] : 0) << 16;
                        offset++; context->tires[n].sensor_id |= (offset < availn ? context->frame_data[offset] : 0) << 8 ;
                        offset++; context->tires[n].sensor_id |= (offset < availn ? context->frame_data[offset] : 0) << 0 ;
                        offset++; context->tires[n].pressure   = (offset < availn ? context->frame_data[offset] : 0) << 8 ;
                        offset++; context->tires[n].pressure  |= (offset < availn ? context->frame_data[offset] : 0) << 0 ;
                        offset++; context->tires[n].temperature= (offset < availn ? context->frame_data[offset] : 0) << 0 ;
                        offset++; context->tires[n].state      = (offset < availn ? context->frame_data[offset] : 0) << 0 ;
                        if (sfn == 0) {
                            context->tires_current++;
                        }
                    }
                    if (sfn != 0 || context->tires_current == context->tires_total) {
                        context->ack_flags &= ~TPMS_TIRES_ACK;
                        if (context->callback) {
                            context->callback(context, mfn, sfn);
                        }
#ifdef ENABLE_TPMS_JNI
                        if (env && context->jcls_tpms && context->jobj_tpms && context->jmid_callback) {
                            env->CallVoidMethod(context->jobj_tpms, context->jmid_callback, mfn, sfn);
                        }
#endif
                    }
                    break;
                case 0x65:
                    if (sfn == 0xaa) {
                        context->ack_flags &= ~TPMS_UNWATCH_ACK;
                    }
                    break;
                }
                availn -= length;
                find   += length;
            }
        }
    }

#ifdef ENABLE_TPMS_JNI
    // need call DetachCurrentThread
    g_jvm->DetachCurrentThread();
#endif

    return NULL;
}

// 函数实现
void* tpms_init(char *dev, PFN_TPMS_CB callback)
{
    TPMS_CONTEXT *context = (TPMS_CONTEXT*)calloc(1, sizeof(TPMS_CONTEXT));
    if (!context) {
        printf("failed to allocate memory for tpms context !\n");
        return NULL;
    }

    // init serial port
    context->fd = open(dev, O_RDWR | O_NOCTTY);
    if (context->fd < 0) {
        printf("failed to open serial port !\n");
    }
    else {
        struct termios ti;
        tcgetattr(context->fd, &ti);
        cfmakeraw(&ti);
        cfsetospeed(&ti, B9600);
        cfsetospeed(&ti, B9600);
        tcsetattr(context->fd, TCSANOW, &ti);
        tcflush  (context->fd, TCIOFLUSH);
    }

    // for callback
    context->callback = callback;

    // create data receive thread
    pthread_create(&context->thread_tpms, NULL, tpms_data_thread, context);

    return context;
}

void tpms_free(void *ctxt)
{
    TPMS_CONTEXT *context = (TPMS_CONTEXT*)ctxt;
    if (!context) return;

    context->state |= TPMS_THREAD_EXIT;
    close(context->fd);
    pthread_join(context->thread_tpms, NULL);

#ifdef ENABLE_TPMS_JNI
    get_jni_env()->DeleteGlobalRef(context->jobj_tpms);
#endif

    free(context);
}

void* tpms_get_params(void *ctxt, int type)
{
    TPMS_CONTEXT *context = (TPMS_CONTEXT*)ctxt;
    if (!context) return NULL;
    switch (type) {
    case TPMS_TYPE_ALERT: return context->alerts;
    case TPMS_TYPE_TIRES: return context->tires ;
    }
    return NULL;
}

int tpms_handshake(void *ctxt)
{
    char frame[TXRX_BUF_LEN];
    int  flen    = 0;
    int  timeout = 0;
    int  ret;

    TPMS_CONTEXT *context = (TPMS_CONTEXT*)ctxt;
    if (!context) return -1;

    context->ack_flags |= TPMS_HANDSHAKE_ACK;
    flen = make_tpms_frame(frame, 0x11, 0x63, NULL, 0);
    ret  = write(context->fd, frame, flen);
    if (ret == -1) {
        printf("failed to write frame !\n");
        return -1;
    }
    while (timeout++ < 20 && (context->ack_flags & TPMS_HANDSHAKE_ACK)) usleep(50*1000);
    if (context->ack_flags & TPMS_HANDSHAKE_ACK) {
        printf("wait handshake ack timeout !\n");
        return -1;
    }
    return 0;
}

int tpms_config_alert(void *ctxt, int i, TPMS_ALERT *alert)
{
    char frame[TXRX_BUF_LEN];
    int  flen    = 0;
    int  dlen    = 0;
    int  timeout = 0;
    char data[MAX_ALERT_NUM*2];
    int  ret, j;

    TPMS_CONTEXT *context = (TPMS_CONTEXT*)ctxt;
    if (!context) return -1;

    if (i == 0) {
        for (j=0; j<MAX_ALERT_NUM; j++) {
            if (j != MAX_ALERT_NUM - 1) {
                data[j*2 + 0] = alert[j].pressure_hot;
                data[j*2 + 1] = alert[j].pressure_low;
            }
            else {
                data[j*2 + 0] = alert[j].pressure_hot;
            }
        }
    }
    else {
        if (i != MAX_ALERT_NUM - 1) {
            data[0] = alert[0].pressure_hot;
            data[1] = alert[0].pressure_low;
        }
        else {
            data[0] = alert[0].pressure_hot;
        }
    }

    context->ack_flags |= TPMS_ALERT_ACK;
    dlen = (i == 0) ? MAX_ALERT_NUM*2 - 1 : 2;
    flen = make_tpms_frame(frame, 0x62, i, data, dlen);
    ret  = write(context->fd, frame, flen);
    if (ret == -1) {
        printf("failed to write frame !\n");
        return -1;
    }
    while (timeout++ < 20 && (context->ack_flags & TPMS_ALERT_ACK)) usleep(50*1000);
    if (context->ack_flags & TPMS_ALERT_ACK) {
        printf("wait alert ack timeout !\n");
        return -1;
    }

    if (i == 0) {
        for (j=0; j<MAX_ALERT_NUM; j++) {
            if (j != MAX_ALERT_NUM - 1) {
                context->alerts[j].pressure_hot = alert[j].pressure_hot;
                context->alerts[j].pressure_hot = alert[j].pressure_low;
            }
            else {
                context->alerts[j].pressure_hot = alert[j].pressure_hot;
            }
        }
    }
    else {
        if (i != MAX_ALERT_NUM - 1) {
            context->alerts[i - 1].pressure_hot = alert[0].pressure_hot;
            context->alerts[i - 1].pressure_low = alert[0].pressure_low;
        }
        else {
            context->alerts[i - 1].pressure_hot = alert[0].pressure_hot;
        }
    }

    return 0;
}

int tpms_request_alert(void *ctxt, int i)
{
    char frame[TXRX_BUF_LEN];
    int  flen    = 0;
    int  timeout = 0;
    int  ret;

    TPMS_CONTEXT *context = (TPMS_CONTEXT*)ctxt;
    if (!context) return -1;

    context->ack_flags |= TPMS_ALERT_ACK;
    flen = make_tpms_frame(frame, 0x62, i, NULL, 0);
    ret  = write(context->fd, frame, flen);
    if (ret == -1) {
        printf("failed to write frame !\n");
        return -1;
    }
    while (timeout++ < 20 && (context->ack_flags & TPMS_ALERT_ACK)) usleep(50*1000);
    if (context->ack_flags & TPMS_ALERT_ACK) {
        printf("wait alert ack timeout !\n");
        return -1;
    }
    return 0;
}

int tpms_request_tire(void *ctxt, int i)
{
    char frame[TXRX_BUF_LEN];
    int  flen    = 0;
    int  timeout = 0;
    int  ret;

    TPMS_CONTEXT *context = (TPMS_CONTEXT*)ctxt;
    if (!context) return -1;

    context->ack_flags |= TPMS_TIRES_ACK;
    flen = make_tpms_frame(frame, 0x63, i, NULL, 0);
    ret  = write(context->fd, frame, flen);
    if (ret == -1) {
        printf("failed to write frame !\n");
        return -1;
    }
    while (timeout++ < 20 && (context->ack_flags & TPMS_TIRES_ACK)) usleep(50*1000);
    if (context->ack_flags & TPMS_TIRES_ACK) {
        printf("wait tire ack timeout !\n");
        return -1;
    }
    return 0;
}

int tpms_unwatch_tire(void *ctxt, int i)
{
    char frame[TXRX_BUF_LEN];
    int  flen    = 0;
    int  timeout = 0;
    int  ret;

    TPMS_CONTEXT *context = (TPMS_CONTEXT*)ctxt;
    if (!context) return -1;

    context->ack_flags |= TPMS_UNWATCH_ACK;
    flen = make_tpms_frame(frame, 0x65, i, NULL, 0);
    ret  = write(context->fd, frame, flen);
    if (ret == -1) {
        printf("failed to write frame !\n");
        return -1;
    }
    while (timeout++ < 20 && (context->ack_flags & TPMS_UNWATCH_ACK)) usleep(50*1000);
    if (context->ack_flags & TPMS_UNWATCH_ACK) {
        printf("wait unwatch ack timeout !\n");
        return -1;
    }
    return 0;
}

int tpms_learn_tire(void *ctxt, int i)
{
    char frame[TXRX_BUF_LEN];
    int  flen    = 0;
    int  timeout = 0;
    int  ret;

    TPMS_CONTEXT *context = (TPMS_CONTEXT*)ctxt;
    if (!context) return -1;

    flen = make_tpms_frame(frame, 0x66, i, NULL, 0);
    ret  = write(context->fd, frame, flen);
    if (ret == -1) {
        printf("failed to write frame !\n");
        return -1;
    }
    return 0;
}

int tpms_dump(void *ctxt)
{
    int i;
    TPMS_CONTEXT *context = (TPMS_CONTEXT*)ctxt;
    if (!context) return -1;

    printf("\n");
    printf("+------------------+\n");
    printf(" dump tpms context  \n");
    printf("+------------------+\n");

    printf("fd          : %d\n"  , context->fd);
    printf("thread_tpms : %p\n"  , (void*)context->thread_tpms);
    printf("state       : %08X\n", context->state    );
    printf("ack_flags   : %08X\n", context->ack_flags);
    printf("callback    : %p\n"  , context->callback );
    printf("\n");

    printf("tires  : id      pressure  temp  state\n");
    for (i=0; i<MAX_TIRES_NUM; i++) {
        printf("%d        %06X  %-4d      %-3d   %02X\n", i,
            context->tires[i].sensor_id,
            context->tires[i].pressure,
            context->tires[i].temperature,
            context->tires[i].state);
    }
    printf("\n");

    printf("alerts : hot  low\n");
    for (i=0; i<MAX_ALERT_NUM; i++) {
        printf("%d        %-3d   %-3d\n", i,
            context->alerts[i].pressure_hot,
            context->alerts[i].pressure_low);
    }
    printf("\n");

    printf("frame_data:\n");
    for (i=0; i<TXRX_BUF_LEN; i++) {
        printf("%02X ", context->frame_data[i]);
        if (i % 16 == 15) printf("\n");
    }
    printf("\n\n");
    return 0;
}

#ifdef ENABLE_TPMS_JNI
void tpms_init_jni_callback(void *ctxt, JNIEnv *env, jobject obj)
{
    TPMS_CONTEXT *context = (TPMS_CONTEXT*)ctxt;
    if (!context) return;
    context->jcls_tpms     = env->GetObjectClass(obj);
    context->jobj_tpms     = env->NewGlobalRef(obj);
    context->jmid_callback = env->GetMethodID(context->jcls_tpms, "internalCallback", "(II)V");
}
#endif

#ifdef ENABLE_TPMS_TEST
int main(int argc, char *argv[])
{
    char *uart = (char*)"/dev/ttyS1";
    void *tpms = NULL;
    char line[256];
    char cmd [16];
    char arg0[16];
    char arg1[16];
    char arg2[16];

    if (argc >= 2) {
        uart = argv[1];
    }

    tpms = tpms_init(uart, NULL);
    if (!tpms) {
        printf("failed to init tpms !\n");
        return 0;
    }

    while (1) {
        fgets(line, sizeof(line), stdin);
        sscanf(line, "%s %s %s %s", cmd, arg0, arg1, arg2);
        if (strcmp(cmd, "exit") == 0) break;
        else if (strcmp(cmd, "handshake") == 0) {
            tpms_handshake(tpms);
        }
        else if (strcmp(cmd, "config_alert") == 0) {
            TPMS_ALERT alert;
            alert.pressure_hot = atoi(arg1);
            alert.pressure_low = atoi(arg2);
            tpms_config_alert(tpms, atoi(arg0), &alert);
        }
        else if (strcmp(cmd, "request_alert") == 0) {
            tpms_request_alert(tpms, atoi(arg0));
        }
        else if (strcmp(cmd, "request_tire") == 0) {
            tpms_request_tire(tpms, atoi(arg0));
        }
        else if (strcmp(cmd, "unwatch_tire") == 0) {
            tpms_unwatch_tire(tpms, atoi(arg0));
        }
        else if (strcmp(cmd, "learn_tire") == 0) {
            tpms_learn_tire(tpms, atoi(arg0));
        }
        else if (strcmp(cmd, "dump") == 0) {
            tpms_dump(tpms);
        }
        line[0] = cmd[0] = arg0[0] = arg1[0] = arg2[0] = '\0';
    }

    tpms_free(tpms);
    return 0;
}
#endif

