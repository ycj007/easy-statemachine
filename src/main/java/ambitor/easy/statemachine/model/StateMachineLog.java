package ambitor.easy.statemachine.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class StateMachineLog {
    private Integer id;
    private String machineCode;
    private String source;
    private String target;
    private String event;
    private String transitionResult;
    private Date createTime;
    private Date updateTime;
    private String response;
}