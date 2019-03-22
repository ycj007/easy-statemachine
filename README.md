# 状态机工作流框架

### 前话

年初开始和一班子兄弟在创业公司做信贷业务。开始人多，自己只负责还款功能，后来公司战线拉开人员调动比较大，放款也由我负责，摆在眼前的需求是接入两个新渠道方，放款流程也有所变化，阅读过之前的放款代码后发现很多可以优化的点，时间紧迫，花了一周时间晚上回家加班到凌晨写了一个基于状态机的工作流框架，经过一段时间完善与验证，目前已经推广到公司所有项目组。能写出一些通用框架提高团队开发效率还是很开心的，现在单独拎出来维护到GitHub做一个开源工作流框架。

### 优化效果

-   放款时间从原来的5分钟缩短为1~2分钟左右
-   流程完全解耦抽象，新接入渠道的放款开发最快半天搞定（是不是很棒，不要告诉我老板）
-   方便新人熟悉流程，节省学习成本，代码中有完整清晰的放款流程节点定义
-   方便查错，一眼就能看出来放款流程卡在哪个节点，每个节点操作日志自动落库
-   ...

### 场景介绍

信贷业务主要流程为`用户注册->风控系统授信->风控出额度->用户申请放款->用户还清贷款`，其中放款功能的简易流程如下`开立二类户->建档授信->签署合同->放款->出金`，其中出金还包括复杂子流程，流程图如下：

![](https://oscimg.oschina.net/oscnet/dd5c192581ab6610ea1a64521b28636ff3f.jpg)

                                                   （放款流程图）

![](https://oscimg.oschina.net/oscnet/f30365910938757d659f6a6a1f7dbc0657b.jpg)

                                                    （出金流程图）

此类信贷业务是一个典型的工作流业务，并且针对不同渠道/流量方的接入放款的流程不同，需要重新定义流程，如下：

-   A渠道：`开立二类户->建档授信->等待建档授信回调->签署合同->放款->出金`
-   B渠道：`创建客户号->建档授信->等待建档授信回调->放款->出金`
-   C渠道：`开立二类户->建档授信->等待建档授信回调->放款->出金`
-   others...

### 第一版实现

第一版设计通过一张Task表实现，把每一步都抽象成一个task任务，每个任务有`任务类型、执行时间、重试次数、任务状态、请求参数、返回参数`等字段，然后有一个分布式的调度系统XXL-JOB(基于Quartz)不断的扫描这张表，把所有需要执行的task任务放到MQ并标记状态为Running，然后在消费者中会有任务类型对应的处理器Processor去执行相应业务，每次执行完后在上一个Processor中创建下一个任务task，整个流程的状态控制是在task的Processor中控制的。举例：

-   创建一条task_type为CreateCardII（开二类户）的task任务记录
-   XXL-JOB触发任务生产者，扫描到CreateCardII的task任务放入MQ
-   MQ消费者根据task_type找到对应的CreateCardIIProcessor（开二类户处理器）进行处理
-   开户成功则在CreateCardIIProcessor（开二类户处理器）中创建documentCredit（建档授信）的task任务，流程继续
-   开户失败则在CreateCardIIProcessor（开二类户处理器）中创建IncomingBack放款失败回调task任务，流程终止，放款失败
-   其它类似流程...

![](https://oscimg.oschina.net/oscnet/1ac799117e571540ad6389f700bc10a51db.jpg)

（一笔放款完整的Task）

### 第一版的问题

Q1.流程节点间代码完全耦合，无法适应易变的流程

A1.流程中开二类户节点的下一个节点完全在代码中用硬代码`if else`写死，如果新渠道放款流程为`创建客户号->放款->出金`，改造成本太高，后期维护工作量大，无法适应节点顺序随意变化，伪代码：

```
class 二类户处理类 {
    if( 开二类户成功 ) {
        创建建档授信task任务
    } else{
        创建放款失败回调task任务
    }
}

```

Q2.放款时间长，生成Task任务多

A2.由于XXL-JOB调度器是X秒调度一次，一次执行Y条，因为节点耦合每次调度一次只能执行一个节点的Task，执行完后生成的下一个节点Task只能下一次调度的时候才能触发，目前一个放款有15条Task，大致需要5~10分钟，随着量越来越大，task越来越多，放款时间将越来越久。

Q3.放款流程当前在哪个节点状态不明确

A3.由于分成15条Task去做，资产表状态只有父级状态`放款中、还款中、结清...`，不能很好区分当前资产处在放款中的子状态，比如`开二类户中、建档授信...`，不仅不利于问题排查，也不利于实时统计的细化(看需求)

Q4.流程运行时节点之间的Session数据共享问题

A4.流程节点之间往往可能需要数据共享，比如放款人信息在开二类户已经查出来，下一个建档授信节点应该不用再去查询，原Task实现方式只能把信息保存在下一个建档授信Task的request_data字段中，然后再查出来，但很多数据共享的场景本身更适合在内存中共享而不是数据库

Q5.新人学习成本很大

A5.以前分成15条Task实现的方式，没有地方写明每条Task的依赖关系，先后顺序等，导致新人来了之后除了看viso流程图，在代码中必须切入每个Task源码才能熟悉流程，刚开始就切入整个细节实现，学习成本高也非常繁琐，而工作流框架在状态机实现类中清晰定义了每个流程节点以及转换关系，通过后续简单的扩展可以支持XML、HTML配置工作流

6.Others...

### 工作流框架实现

上面提到了诸如`代码解耦、高效率开发/维护、节省开发成本`等一系列好处，下面谈谈工作流的实现，整个工作流是基于状态机实现的，介绍工作流之前先介绍下状态机的概念

#### 有限状态机定义

有限状态机，（英语：Finite-state machine, FSM），又称有限状态自动机，简称状态机，是表示有限个状态以及在这些状态之间的转移和动作等行为的数学模型。有限状态机体现了两点：首先是离散的，然后是有限的。以下是对状态机抽象定义

State（状态）：构成状态机的基本单位。 状态机在任何特定时间都可处于某一状态。从生命周期来看有`Initial State、End State、Suspend State(挂起状态)`

Event（事件）：导致转换发生的事件活动

Transitions（转换器）：两个状态之间的定向转换关系，状态机对发生的特定类型事件响应后当前状态由A转换到B。`标准转换、选择转、子流程转换`多种抽象实现

Actions（转换操作）：在执行某个转换时执行的具体操作。

Guards（检测器）：检测器出现的原因是为了转换操作执行后检测结果是否满足特定条件从一个状态切换到某一个状态

Interceptor（拦截器）：对当前状态改变前、后进行监听拦截。如：每个状态变更后插入日志等

![](https://oscimg.oschina.net/oscnet/9724f02b886173ed28f7a802e1f20e1f40c.jpg)

                                 状态机扭转图

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
