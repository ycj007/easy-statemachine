package ambitor.easy.statemachine.core.builder;

/**
 * 类似StringBuilder <I> 需要拼接的对象
 * Created by Ambitor on 2019-01-21.
 */
public interface ConfigurerBuilder<I> {
    /**
     * 拼接方法
     * @return 需要拼接的类
     */
    I and();
}
