#ifndef __TPMS_H__
#define __TPMS_H__

#ifdef __cplusplus
extern "C" {
#endif

// 常量定义
#define MAX_TIRES_NUM   5
#define MAX_ALERT_NUM   6

// 类型定义
typedef struct {
    int pressure_hot; // pressure high or temperature
    int pressure_low; // pressure low
} TPMS_ALERT;

typedef struct {
    int sensor_id;
    int pressure;
    int temperature;
    int state;
} TPMS_TIRE;

enum {
    TPMS_TYPE_ALERT = 0x62,
    TPMS_TYPE_TIRES = 0x63,
    TPMS_TYPE_LEARN = 0x66,
};

typedef void (*PFN_TPMS_CB)(void *ctxt, int type, int i);

// 函数声明
void* tpms_init(char *dev, PFN_TPMS_CB callback);
void  tpms_free(void *ctxt);
void* tpms_get_params   (void *ctxt, int type);
int   tpms_handshake    (void *ctxt);
int   tpms_config_alert (void *ctxt, int i, TPMS_ALERT *alert);
int   tpms_request_alert(void *ctxt, int i);
int   tpms_request_tire (void *ctxt, int i);
int   tpms_unwatch_tire (void *ctxt, int i);
int   tpms_learn_tire   (void *ctxt, int i);
int   tpms_dump         (void *ctxt);

#ifdef __cplusplus
}
#endif

#endif

