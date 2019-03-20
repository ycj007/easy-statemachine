package ambitor.easy.statemachine.core.transition.config;


import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.exception.StateMachineException;
import ambitor.easy.statemachine.core.guard.DefaultGuard;
import ambitor.easy.statemachine.core.guard.Guard;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认选择转换器配置类
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public class DefaultChoiceTransitionConfigurer<S, E> extends BaseTransitionConfigurer<S, E> implements ChoiceTransitionConfigurer<S, E> {

    private ChoiceData<S, E> first;
    private List<ChoiceData<S, E>> thens = new ArrayList<>();
    private ChoiceData<S, E> last;

    /**
     * 等同于if
     * @param target 则状态变更成target
     * @param guard  如果返回true
     * @return
     */
    @Override
    public ChoiceTransitionConfigurer<S, E> first(S target, Guard<S, E> guard) {
        first = new ChoiceData<>(getSource(), target, guard);
        return this;
    }

    /**
     * 等同于 else if
     * @param target 则状态变更成target
     * @param guard  如果返回true
     * @return
     */
    @Override
    public ChoiceTransitionConfigurer<S, E> then(S target, Guard<S, E> guard) {
        thens.add(new ChoiceData<>(getSource(), target, guard));
        return this;
    }

    /**
     * 等同于 else
     * @param target 如果first then 都不使用，变更状态target
     */
    @Override
    public ChoiceTransitionConfigurer<S, E> last(S target) {
        last = new ChoiceData<>(getSource(), target, new DefaultGuard<>());
        return this;
    }

    /**
     * action执行成功，状态扭转 source -> target
     * 如抛出异常则状态不会扭转
     * @param action 事件触发后执行的action
     */
    @Override
    public ChoiceTransitionConfigurer<S, E> action(Action<S, E> action) {
        addAction(action);
        return this;
    }

    /**
     * action执行成功状态从 source -> target
     * 注意，如果有error函数，当action执行失败时会调用error，且如果error执行成功无异常后
     * 状态也会扭转 source -> target
     * @param action
     * @param error
     */
    @Override
    public ChoiceTransitionConfigurer<S, E> action(Action<S, E> action, Action<S, E> error) {
        addAction(action, error);
        return this;
    }

    /**
     * 源状态
     * @param source 源状态S
     */
    @Override
    public ChoiceTransitionConfigurer<S, E> source(S source) {
        setSource(source);
        return this;
    }

    /**
     * 触发状态扭转 Source -> Target 的事件
     * @param event 事件E
     */
    @Override
    public ChoiceTransitionConfigurer<S, E> event(E event) {
        setEvent(event);
        return this;
    }

    public List<ChoiceData<S, E>> config() {
        if (first == null) {
            throw new StateMachineException("ChoiceTransitionConfigurer must defined first() choice");
        }
        if (last == null) {
            throw new StateMachineException("ChoiceTransitionConfigurer must defined last() choice");
        }
        List<ChoiceData<S, E>> choiceData = new ArrayList<>();
        choiceData.add(first);
        if (thens != null) {
            choiceData.addAll(thens);
        }
        choiceData.add(last);
        if (choiceData.isEmpty()) {
            throw new StateMachineException("ChoiceTransitionConfigurer defined error ");
        }
        return choiceData;
    }

}
