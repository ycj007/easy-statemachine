package ambitor.easy.statemachine.sf.action;


import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.context.StateContext;
import ambitor.easy.statemachine.sf.enumerate.SFGrantEvent;
import ambitor.easy.statemachine.sf.enumerate.SFGrantState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SFGrantAction implements Action<SFGrantState, SFGrantEvent> {

    /**
     * 放款
     * @param context 上下文
     */
    @Override
    public void execute(StateContext<SFGrantState, SFGrantEvent> context) {
        System.out.println("放款");
    }

}
