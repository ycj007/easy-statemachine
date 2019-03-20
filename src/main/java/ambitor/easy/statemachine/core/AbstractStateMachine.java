package ambitor.easy.statemachine.core;

import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.context.DefaultMessage;
import ambitor.easy.statemachine.core.context.DefaultStateContext;
import ambitor.easy.statemachine.core.context.Message;
import ambitor.easy.statemachine.core.context.MessageHeaders;
import ambitor.easy.statemachine.core.context.StateContext;
import ambitor.easy.statemachine.core.exception.StateMachineException;
import ambitor.easy.statemachine.core.interceptor.StateMachineInterceptorList;
import ambitor.easy.statemachine.core.state.State;
import ambitor.easy.statemachine.core.transition.Transition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;

/**
 * 状态机抽象类
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
@Slf4j
public class AbstractStateMachine<S, E> implements StateMachine<S, E> {
    //所有状态
    private final Map<S, State<S, E>> states;
    //所有转换器
    private final Map<S, Collection<Transition<S, E>>> transitions;
    //初始化状态
    private final State<S, E> initialState;
    //当前转换器
    private volatile Transition<S, E> currentTransition;
    //当前状态
    private volatile State<S, E> currentState;
    //当前事件
    private volatile Message<E> currentEvent;
    //当前发生的异常
    private volatile Exception currentError;
    //拦截器
    private final StateMachineInterceptorList<S, E> interceptors;


    public AbstractStateMachine(Map<S, State<S, E>> states, Map<S, Collection<Transition<S, E>>> transitions,
                                State<S, E> initialState, State<S, E> currentState, Exception currentError,
                                StateMachineInterceptorList<S, E> interceptors) {
        this.states = states;
        this.transitions = transitions;
        this.initialState = initialState;
        this.currentState = currentState;
        this.currentError = currentError;
        this.interceptors = interceptors;
    }

    @Override
    public State<S, E> getInitialState() {
        return initialState;
    }

    @Override
    public void setStateMachineError(Exception exception) {
        this.currentError = exception;
    }

    @Override
    public Exception getStateMachineError() {
        return currentError;
    }

    @Override
    public boolean sendEvent(Message<E> event) {
        return sendEvent(event, false);
    }

    @Override
    public boolean sendEvent(E event) {
        return sendEvent(new DefaultMessage<>(event, null));
    }

    @Override
    public boolean start() {
        return start(null);
    }

    @Override
    public boolean start(MessageHeaders headers) {
        E event = getCurrentEvent();
        boolean accepted = sendEvent(new DefaultMessage<>(event, headers), true);
        return accepted && (currentState.isEnd() || currentState.isSuspend());
    }

    @Override
    public State<S, E> getState() {
        return currentState;
    }

    /**
     * 获取当前事件
     */
    @Override
    public Message<E> getEvent() {
        return currentEvent;
    }

    @Override
    public void resetStateMachine(S newState) {
        if(newState==null) throw new StateMachineException("状态不能为空");
        this.currentState = states.get(newState);
    }

    @Override
    public Collection<State<S, E>> getStates() {
        if (states == null) {
            return null;
        }
        return states.values();
    }

    @Override
    public Map<S, Collection<Transition<S, E>>> getTransitions() {
        return transitions;
    }

    /**
     * 当前转换器
     */
    @Override
    public Transition<S, E> transition() {
        return currentTransition;
    }

    @Override
    public boolean isComplete() {
        return currentState.isEnd() || currentState != null;
    }

    @Override
    public StateMachineInterceptorList<S, E> interceptors() {
        return interceptors;
    }

    /**
     * 发送事件
     * @param event     事件
     * @param autoDrive 是否自动触发事件扭转状态
     * @return 是否接受事件
     */
    private boolean sendEvent(Message<E> event, boolean autoDrive) {
        boolean accepted = sendEventInternal(event);
        if (accepted && !currentState.isEnd()) {
            //自动驱动流程
            if (autoDrive) {
                //如果不是最终状态、不是挂起状态、并且状态机接受了上一事件
                while (!currentState.isEnd() && accepted && !currentState.isSuspend()) {
                    E nextEvent = null;
                    for (Transition<S, E> transition : transitions.get(currentState.getId())) {
                        if (transition.getSource().getId().equals(currentState.getId())) {
                            nextEvent = transition.getEvent();
                            break;
                        }

                    }
                    Message<E> newMessage = new DefaultMessage<>(nextEvent, event.getHeaders());
                    accepted = sendEventInternal(newMessage);
                }
            }
        }
        return accepted;
    }


    /**
     * 发送事件
     * @param event
     * @return 是否接受事件
     */
    private boolean sendEventInternal(Message<E> event) {
        try {
            currentEvent = event;
            event = interceptors.preEvent(event, this);
            //找出当前状态所有的转换器
            Collection<Transition<S, E>> trans = transitions.get(currentState.getId());
            if (!CollectionUtils.isEmpty(trans)) {
                //事件的action是否已经执行
                boolean transitExecuted = false;
                for (Transition<S, E> transition : trans) {
                    //如果事件不是状态关心的
                    if (!transition.getEvent().equals(event.getPayload())) {
                        continue;
                    }
                    //设置当前转换器
                    currentTransition = transition;
                    /*  进入以下流程表示已经找到Transition */
                    StateContext<S, E> stateContext = new DefaultStateContext<>(event, transition, transition.getSource(), transition.getTarget());
                    //如果transition.transit()没有执行过
                    if (!transitExecuted) {
                        //转换改变前拦截器
                        interceptors.preStateChange(currentState, event, transition, this);
                        //状态扭转前执行action, action执行失败表示不接受事件，返回false
                        boolean accept = transition.transit(stateContext);
                        //标记已执行
                        transitExecuted = true;
                        if (!accept) {
                            setStateMachineError(stateContext.getException());
                            log.info("状态扭转失败,source {} -> target {} Event {}", currentState.getId(), transition.getTarget().getId(), event.getPayload());
                            //状态机发生异常
                            interceptors.stateMachineError(this, stateContext.getException());
                            return false;
                        }
                    }
                    //已经执行过transition.transit(),不用再执行，只需要判断guard()
                    //StandardTransition只有一个默认的guard，返回true
                    //ChoiceTransition 每个if/elseif分支有一个guard，但configurer已经确保必须有一个else分支使用guard返回true
                    if (transition.guard().evaluate(stateContext)) {
                        //转换成功
                        return transitionSuccess(transition, stateContext, event);
                    }
                }
            }
            String msg = MessageFormat.format("没有找到Transition, 状态{0}，事件{1}", currentState.getId(), event.getPayload());
            log.info(msg);
            setStateMachineError(new StateMachineException(msg));
            return false;
        } catch (Exception e) {
            setStateMachineError(e);
            currentError = e;
            log.error("发送事件异常，未接受该事件" + event, e);
            if (interceptors != null) {
                interceptors.stateMachineError(this, currentError);
            }
            return false;
        }
    }

    /**
     * 转换成功
     */
    private boolean transitionSuccess(Transition<S, E> transition, StateContext<S, E> stateContext, Message<E> event) {
        log.info("状态扭转成功,source {} -> target {}", currentState.getId(), transition.getTarget().getId());
        //触发state退出绑定的action
        fireStateAction(currentState.getExitActions(), stateContext);
        //扭转状态
        currentState = transition.getTarget();
        //触发state进入绑定的action
        fireStateAction(currentState.getEntryActions(), stateContext);
        //状态改变后
        interceptors.afterStateChange(currentState, event, transition, this);
        return true;
    }

    /**
     * 触发状态绑定的action
     */
    private void fireStateAction(Collection<? extends Action<S, E>> actions, StateContext<S, E> context) {
        if (!CollectionUtils.isEmpty(actions)) {
            for (Action<S, E> action : actions) {
                action.execute(context);
            }
        }
    }

    private E getCurrentEvent() {
        Collection<Transition<S, E>> trans = transitions.get(currentState.getId());
        if (!CollectionUtils.isEmpty(trans)) {
            for (Transition<S, E> transition : trans) {
                if (transition.getSource().getId().equals(getState().getId())) {
                    return transition.getEvent();
                }
            }
        }
        return null;
    }
}
