package ambitor.easy.statemachine.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class StateMachineTask {
    private Integer id;
    private String machineCode;
    private String machineState;
    private String machineType;
    private String scanStatus;
    private String transactionId;
    private Integer currentTrytimes;
    private Integer retryTimes;
    private Date nextRunTime;
    private Date createTime;
    private Date updateTime;
    private String requestData;
    private String responseData;

    public boolean isLastRetry() {
        return getCurrentTrytimes() >= getRetryTimes();
    }
}