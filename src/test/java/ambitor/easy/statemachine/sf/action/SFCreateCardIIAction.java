package ambitor.easy.statemachine.sf.action;


import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.context.StateContext;
import ambitor.easy.statemachine.sf.enumerate.SFGrantEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ambitor.easy.statemachine.sf.enumerate.SFGrantState;

@Slf4j
@Component
public class SFCreateCardIIAction implements Action<SFGrantState, SFGrantEvent> {

    public void execute(StateContext<SFGrantState, SFGrantEvent> context) {
        log.info("开二类户");
    }


}
