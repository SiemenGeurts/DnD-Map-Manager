package actions;

import app.Constants;
import data.mapdata.Entity;

public class MovementAction extends Action {

    GuideLine guideline;
    protected Entity entity;

    public MovementAction(GuideLine guideline, Entity entity) {
        super(0);
        this.guideline = guideline;
        this.entity = entity;
    }

    public MovementAction(GuideLine guideline, Entity entity, float delay, Object... args) {
        super(delay, args);
        this.guideline = guideline;
        this.entity = entity;
    }

    @Override
    public void execute() {}

    @Override
    public void update(float dt) {
        delay -= dt;
        if(delay < 0) {
            if (!guideline.hasArrived())
                entity.setLocation(guideline.follow(Constants.ANIM_SPEED * dt));
            else if(!finished) {
                finished = true;
                execute();
                detach();
                if(next != null)
                    next.attach();
            }
        }
    }
    
    @Override
    public String toString() {
    	return "MovementAction [id=" + entity.getID() + " p1=" + guideline.path.get(0) + " p2=" + guideline.path.get(guideline.path.size()-1) + "]";
    }
}