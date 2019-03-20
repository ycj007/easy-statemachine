package ambitor.easy.statemachine.sf.action;

import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.context.StateContext;
import ambitor.easy.statemachine.sf.enumerate.SFGrantEvent;
import ambitor.easy.statemachine.sf.enumerate.SFGrantState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SFDocumentCreditAction implements Action<SFGrantState, SFGrantEvent> {

    @Override
    public void execute(StateContext<SFGrantState, SFGrantEvent> context) {
        log.info("建档授信");
    }

    public Action<SFGrantState, SFGrantEvent> errorAction() {
        return (s) -> log.info("建档授信异常，最后一次尝试");
    }
}
