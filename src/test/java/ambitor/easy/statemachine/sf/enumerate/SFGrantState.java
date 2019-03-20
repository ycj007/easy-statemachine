package ambitor.easy.statemachine.sf.enumerate;

/**
 * 360放款状态流程
 */
public enum SFGrantState {
    //等待校验 WAIT_CHECK,
    //校验失败 CHECK_FAILED,
    //等待开二类户
    WAIT_CREATE_CARDII,
    //开二类户失败
    CREATE_CARDII_FAILED,
    //建档授信
    WAIT_DOCUMENT_CREDIT,
    //等待建档授信回调
    WAIT_DOCUMENT_CREDIT_CALLBACK,
    //建档授信失败
    DOCUMENT_CREDIT_FAILED,
    //放款
    WAIT_GRANT,
    //放款失败
    GRANT_FAILED,
    //等待放款校验
    WAIT_GRANT_CHECK,
    //主流程完成
    GRANT_TASK_SAVE,
    //结束流程
    GRANT_SUCCESS
}
