package ambitor.easy.statemachine.core.factory;

import ambitor.easy.statemachine.core.DefaultStateMachine;
import ambitor.easy.statemachine.core.StateMachine;
import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.configurer.StateMachineConfigurer;
import ambitor.easy.statemachine.core.configurer.adapter.AbstractStateMachineConfigurerAdapter;
import ambitor.easy.statemachine.core.configurer.adapter.StateMachineConfigurerAdapter;
import ambitor.easy.statemachine.core.exception.StateMachineException;
import ambitor.easy.statemachine.core.guard.DefaultGuard;
import ambitor.easy.statemachine.core.guard.Guard;
import ambitor.easy.statemachine.core.interceptor.StateMachineInterceptorList;
import ambitor.easy.statemachine.core.state.State;
import ambitor.easy.statemachine.core.state.config.DefaultStateConfigurer;
import ambitor.easy.statemachine.core.transition.StandardTransition;
import ambitor.easy.statemachine.core.transition.Transition;
import ambitor.easy.statemachine.core.transition.config.BaseTransitionConfigurer;
import ambitor.easy.statemachine.core.transition.config.ChoiceData;
import ambitor.easy.statemachine.core.transition.config.DefaultChoiceTransitionConfigurer;
import ambitor.easy.statemachine.core.transition.config.DefaultStandardTransitionConfigurer;
import ambitor.easy.statemachine.core.transition.config.TransitionConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 状态机工厂
 * Created by Ambitor on 2019/1/21
 */
@Slf4j
public class StateMachineFactory {

    private StateMachineFactory() {
    }

    private static Map<String, Transition> transitions = new HashMap<>();


    /**
     * 创建状态机
     * @param <S>
     * @param <E>
     * @return
     */
    public static <S extends Enum<S>, E extends Enum<E>> StateMachine<S, E> build(StateMachineConfigurer<S, E> stateMachineConfigurer) {
        try {
            if (!(stateMachineConfigurer instanceof AbstractStateMachineConfigurerAdapter)) {
                throw new StateMachineException("stateMachineConfigurer must be AbstractStateMachineConfigurerAdapter instance ");
            }
            StateMachineConfigurerAdapter<S, E> stateMachineConfigurerAdapter = (StateMachineConfigurerAdapter<S, E>) stateMachineConfigurer;
            stateMachineConfigurerAdapter.init();
            String stateMachineName = stateMachineConfigurerAdapter.getName();
            //State
            DefaultStateConfigurer<S, E> stateConfigurer = (DefaultStateConfigurer<S, E>) stateMachineConfigurerAdapter.getStateConfigurer();
            Map<S, State<S, E>> stateMaps = stateConfigurer.getStateMaps();
            State<S, E> initialState = stateMaps.get(stateConfigurer.getInitialState());

            //Transition
            Map<S, Collection<Transition<S, E>>> transitions = new HashMap<>();
            //第一个TransitionConfigurer是系统默认的
            TransitionConfigurer<S, E> configurer = ((BaseTransitionConfigurer<S, E>) stateMachineConfigurerAdapter.getTransitionConfigurer()).getNext();

            while (configurer != null) {
                if (configurer instanceof DefaultStandardTransitionConfigurer) {
                    //StandardTransitionConfigurer
                    DefaultGuard<S, E> defaultGuard = new DefaultGuard<>();
                    DefaultStandardTransitionConfigurer<S, E> standard = (DefaultStandardTransitionConfigurer<S, E>) configurer;
                    State<S, E> target = stateMaps.get(standard.getTarget());
                    State<S, E> source = stateMaps.get(standard.getSource());
                    Collection<Transition<S, E>> collection = transitions.computeIfAbsent(source.getId(), k -> new ArrayList<>());
                    Transition<S, E> t = getTransition(stateMachineName, source, target, standard.getEvent(), defaultGuard, standard.getActions());
                    collection.add(t);
                    configurer = standard.getNext();
                } else if (configurer instanceof DefaultChoiceTransitionConfigurer) {
                    //ChoiceTransitionConfigurer
                    DefaultChoiceTransitionConfigurer<S, E> choice = (DefaultChoiceTransitionConfigurer<S, E>) configurer;
                    State<S, E> source = stateMaps.get(choice.getSource());
                    Collection<Transition<S, E>> collection = transitions.computeIfAbsent(source.getId(), k -> new ArrayList<>());
                    List<ChoiceData<S, E>> choiceData = choice.config();
                    for (ChoiceData<S, E> data : choiceData) {
                        State<S, E> target = stateMaps.get(data.getTarget());
                        Transition<S, E> t = getTransition(stateMachineName, source, target, choice.getEvent(), data.getGuard(), choice.getActions());
                        collection.add(t);
                    }
                    configurer = choice.getNext();
                } else {
                    String msg = MessageFormat.format("TransitionConfigurer -> {0} not support", configurer.getClass().getName());
                    throw new StateMachineException(msg);
                }
            }
            //valid Transition and Status
            checkStatus2Transition(stateMaps, transitions);

            //interceptors of the StateMachine
            StateMachineInterceptorList<S, E> interceptors = (StateMachineInterceptorList<S, E>) stateMachineConfigurerAdapter.getInterceptorConfigurer();

            //StateMachine
            return new DefaultStateMachine<>(stateMaps, transitions, initialState, initialState, null, interceptors);
        } catch (Exception e) {
            throw new StateMachineException("创建状态机失败", e);
        }

    }

    /**
     * 校验状态和转换器
     */
    private static <S extends Enum<S>, E extends Enum<E>> void checkStatus2Transition(Map<S, State<S, E>> stateMaps, Map<S, Collection<Transition<S, E>>> transitions) {
        if (stateMaps == null || stateMaps.isEmpty()) {
            throw new StateMachineException("Please defined State By StateMachine StateConfigurer");
        }
        if (transitions == null || transitions.isEmpty()) {
            throw new StateMachineException("Please defined Transition By StateMachine TransitionConfigurer");
        }
        for (S s : stateMaps.keySet()) {
            State<S, E> state = stateMaps.get(s);
            if (CollectionUtils.isEmpty(transitions.get(s)) && !state.isEnd()) {
                //非结束状态必须定义转换器
                String msg = MessageFormat.format("The non ending state must define the Transition by TransitionConfigurer, State -> {0}", s);
                throw new StateMachineException(msg);
            }
        }
    }

    /**
     * 确保Transition不能重复添加
     */
    private static <S, E> Transition<S, E> getTransition(String stateMachineName, State<S, E> source, State<S, E> target, E event, Guard<S, E> guard, Collection<Action<S, E>> actions) {
        if(source == null) throw new StateMachineException("Transition source state can not be null");
        if(target == null) throw new StateMachineException("Transition target state can not be null");
        if(event == null) throw new StateMachineException( "Transition event can not be null");
        String key = stateMachineName + "_" + source.getId() + "_" + target.getId() + "_" + event;
        Transition<S, E> transition = transitions.get(key);
        if (transition == null) {
            transition = new StandardTransition<>(source, target, event, guard, actions);
            transitions.put(key, transition);
        }
        return transition;
    }
}
