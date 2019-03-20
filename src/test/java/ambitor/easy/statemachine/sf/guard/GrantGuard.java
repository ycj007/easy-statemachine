package ambitor.easy.statemachine.sf.guard;


import ambitor.easy.statemachine.core.exception.StateMachineException;
import ambitor.easy.statemachine.core.guard.Guard;
import ambitor.easy.statemachine.model.StateMachineTask;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;

import static ambitor.easy.statemachine.model.StateMachineConstant.TASK_HEADER;

@Slf4j
public class GrantGuard {
    public static <S, E> Guard<S, E> condition(String key, String expect) {
        return (s) -> {
            if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(expect)) {
                throw new StateMachineException("key or expect can not be blank...");
            }
            StateMachineTask task = (StateMachineTask) s.getMessage().getHeaders().getHeader(TASK_HEADER);
            String responseData = task.getResponseData();
            Map<String, Object> result = JSON.parseObject(responseData);
            if (result == null) {
                log.info("GrantGuard result is null response->{}", responseData);
                return false;
            }
            Object actualValue = result.get(key);
            if (actualValue == null) return false;
            return expect.equals(actualValue);
        };
    }
}
