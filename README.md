# easy-statemachine
轻量级状态机工作流框架

#### 状态机工作流

决定使用工作流时参考了市面上很多开源的框架，不是动则近百MB，就是不满足项目实际场景，最后决定自己动手做。工作流有很多中实现方式`顺序流、WF、状态机`，第一种太简单，第二种太重，综合考虑选择使用状态机实现工作流，在选定状态机方式后又参考了Spring Statemachine`（本状态机源码class命名参考Spring Statemachine）`,以下是和Spring Statemachine的一些特性对比

-   去掉了Spring Statemachine复杂的Region特性等...
-   增加了Suspend状态、一键触发全流程功能
-   源码设计上保证同一种工作流所有转换器、事件、状态都是无状态单例
-   流程之间通过SateContext进行Session会话共享
-   当前只实现了Standard Transition、Choice Transition两种转换器，暂不支持子流程转换器、Join Transition、Fork Transition
-   ...

放款流程的配置如下：

```
    /**
     * 定义状态枚举
     */
    public enum SFGrantState {
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

    /**
     * 定义事件枚举
     */
    public enum SFGrantEvent {
        //开二类户
        CREATE_CARDII,
        //建档授信
        DOCUMENT_CREDIT,
        //建档授信回调
        DOCUMENT_CREDIT_CALLBACK,
        //放款
        GRANTED,
        //放款校验
        GRANT_CHECKED,
        //结束
        FINISHED
    }       

    /**
     * 放款工作流配置
     */
    @Slf4j
    @EnableWithStateMachine //集成Spring IOC 的注解
    public class SFGrantStateMachineConfig extends StateMachineConfigurerAdapter<SFGrantState, SFGrantEvent> {

        /**
         * 工作流所有节点状态配置
         */
        @Override
       public void configure(StateConfigurer<SFGrantState, SFGrantEvent> states) {
            states
                    .newStatesConfigurer()
                    // 定义初始状态
                    .initial(SFGrantState.WAIT_CREATE_CARDII)
                    // 定义挂起状态，遇到此状态后状态机自动挂起，直到再次手动触发事件(回调中)，状态机继续往下执行
                    .suspend(SFGrantState.WAIT_DOCUMENT_CREDIT_CALLBACK)
                    // 放款校验状态
                    .suspend(SFGrantState.WAIT_GRANT_CHECK)
                    // 定义其它所有状态集合
                    .states(EnumSet.allOf(SFGrantState.class))
                    //定义结束状态，开二类户失败、建档授信失败、放款失败、放款成功都是结束状态
                    .end(SFGrantState.CREATE_CARDII_FAILED)
                    .end(SFGrantState.DOCUMENT_CREDIT_FAILED)
                    .end(SFGrantState.GRANT_FAILED)
                    .end(SFGrantState.GRANT_SUCCESS);
       }

         /**
         * 状态扭转器配置，配置工作流通过 event（触发事件）把状态从
         * source status（源状态) 转到 target status (目标状态)
         * 可根据上一步建档授信结果转换成不同的 target status (目标状态)
         * 代码完全解耦，把之前在Task中创建下一节点的IF/ELSE抽象成配置信息
         * 具体配置情况如下，细看 2、等待建档授信步骤 
         */
        @Override
        public void configure(TransitionConfigurer<SFGrantState, SFGrantEvent> transitions) {
            transitions
                    /**  1、等待创建二类户  **/
                    .standardTransition()//标准转换器，不具备结果判断，事件触发后只能从X状态转为Y状态
                    .source(SFGrantState.WAIT_CREATE_CARDII)
                    .target(SFGrantState.WAIT_DOCUMENT_CREDIT)
                    .event(SFGrantEvent.CREATE_CARDII)
                    .action(sfCreateCardIIAction, sfCreateCardIIAction.errorAction())
                    .and()

                    /**  2、等待建档授信步骤  **/
                    .choiceTransition()//具备选择结果的转换器，可根据当前事件执行结果扭转到不同状态

                    //原状态为等待建档授信
                    .source(SFGrantState.WAIT_DOCUMENT_CREDIT)

                    //first相当于if，如果建档授信状态返回DOCUMENT_CREDIT_SUCCESS则转换成WAIT_GRANT等待放款
                    .first(SFGrantState.WAIT_GRANT, GrantGuard.condition(DOCUMENT_CREDIT_STATUS, DOCUMENT_CREDIT_SUCCESS))

                    //then相当于elseif，如果建档授信状态返回WAIT_DOCUMENT_CREDIT_CALLBACK则转换成等待建档授信回调
                    .then(SFGrantState.WAIT_DOCUMENT_CREDIT_CALLBACK, GrantGuard.condition(DOCUMENT_CREDIT_STATUS, WAIT_DOCUMENT_CREDIT_CALLBACK))

                    //last相当于else，如果都不是则返回建档授信失败
                    .last(SFGrantState.DOCUMENT_CREDIT_FAILED)

                    //触发事件
                    .event(SFGrantEvent.DOCUMENT_CREDIT)

                    //转换器执行的转换action
                    .action(sfDocumentCreditAction, sfDocumentCreditAction.errorAction())

                    //不同节点转换器的拼接，类似StringBuulder.append()
                    .and()

                    /**  3、等待建档授信回调步骤  **/
                    .choiceTransition()
                    .source(SFGrantState.WAIT_DOCUMENT_CREDIT_CALLBACK)
                    .first(SFGrantState.WAIT_GRANT, GrantGuard.condition(DOCUMENT_CREDIT_STATUS, DOCUMENT_CREDIT_SUCCESS))
                    .last(SFGrantState.DOCUMENT_CREDIT_FAILED)
                    .event(SFGrantEvent.DOCUMENT_CREDIT_CALLBACK)
                    .and()

                    /**  4、等待放款流程 **/
                    .choiceTransition()
                    .source(SFGrantState.WAIT_GRANT)
                    .first(SFGrantState.GRANT_TASK_SAVE, GrantGuard.condition(GRANT_STATUS, GRANT_SUCCESS))
                    .last(SFGrantState.WAIT_GRANT_CHECK)
                    .event(SFGrantEvent.GRANTED)
                    .action(sfGrantAction)
                    .and()

                    /** 5、放款检查流程，如果上一步操作超时 **/
                    .choiceTransition()
                    .source(SFGrantState.WAIT_GRANT_CHECK)
                    .first(SFGrantState.GRANT_TASK_SAVE, GrantGuard.condition(GRANT_STATUS, GRANT_SUCCESS))
                    .last(SFGrantState.GRANT_FAILED)
                    .event(SFGrantEvent.GRANT_CHECKED)
                    .and()

                    /** 6、最后完成的流程 **/
                    .standardTransition()
                    .source(SFGrantState.GRANT_TASK_SAVE).target(SFGrantState.GRANT_SUCCESS)
                    .event(SFGrantEvent.FINISHED)
                    .action(sfFinishAction);
        /**
         * 注册拦截器
         */
        @Override
        public void configure(StateMachineInterceptorConfigurer<SFGrantState, SFGrantEvent> interceptors) {
            //状态改变持久化到数据库拦截器
            interceptors.register(persistStateMachineInterceptor);
        }
    }

```

放款工作流调用代码

```
    //根据工作流名称获取配置信息
    StateMachineConfigurer<S, E> configurer = getByName(task.getMachineType());
    //根据工作流配置创建一个工作流实例
    StateMachine<S, E> stateMachine = StateMachineFactory.build(configurer);
    //开始运行，可传工作流需要参数
    stateMachine.start(params);

```
