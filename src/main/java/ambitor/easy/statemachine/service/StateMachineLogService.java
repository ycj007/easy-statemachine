package ambitor.easy.statemachine.service;

import ambitor.easy.statemachine.model.StateMachineLog;

/**
 * 状态机日志记录
 */
public interface StateMachineLogService {

    int insertSelective(StateMachineLog record);

}