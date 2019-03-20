package ambitor.easy.statemachine.sf.action;

import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.context.StateContext;
import ambitor.easy.statemachine.sf.enumerate.SFGrantEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ambitor.easy.statemachine.sf.enumerate.SFGrantState;

@Slf4j
@Component
public class SFFinishAction implements Action<SFGrantState, SFGrantEvent> {
    /**
     * 放款成功后的action
     */
    @Override
    public void execute(StateContext<SFGrantState, SFGrantEvent> context) {
       log.info("放款成功后action");
    }
}