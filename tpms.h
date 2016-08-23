#ifndef __TPMS_H__
#define __TPMS_H__

// 类型定义
typedef struct {
    int pressure_low;
    int pressure_high;
    int temperature;
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
};

typedef void (*PFN_TIRE_CB)(void *ctxt, int type, int i);

// 函数声明
void* tpms_init(char *dev, PFN_TIRE_CB callback);
void  tpms_free(void *ctxt);
void* tpms_get_params   (void *ctxt, int type);
int   tpms_handshake    (void *ctxt);
int   tpms_config_alert (void *ctxt, int i, TPMS_ALERT *alert);
int   tpms_request_alert(void *ctxt, int i);
int   tpms_request_tire (void *ctxt, int i);
int   tpms_unwatch_tire (void *ctxt, int i);
int   tpms_learn        (void *ctxt, int i);
int   tpms_dump         (void *ctxt);

#endif

