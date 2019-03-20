package ambitor.easy.statemachine.model;

public enum TaskStatus {
    /**
     * 待执行
     */
    open,
    /**
     * 执行中
     */
    running,
    /**
     * 执行失败
     */
    error,
    /**
     * 执行结束
     */
    close,
    /**
     * 添加此状态，用来让任务挂起不被扫描
     */
    suspend;
}
