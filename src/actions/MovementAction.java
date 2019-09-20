package actions;

import java.awt.Point;

import app.Constants;
import data.mapdata.Entity;

public class MovementAction extends Action {

    GuideLine guideline;
    protected Entity entity;
    Point tileLocation;

    public MovementAction(GuideLine guideline, Entity entity) {
        super(0);
        this.guideline = guideline;
        this.entity = entity;
        tileLocation = entity.getTileLocation();
    }

    public MovementAction(GuideLine guideline, Entity entity, float delay, Object... args) {
        super(delay, args);
        this.guideline = guideline;
        this.entity = entity;
        tileLocation = entity.getTileLocation();
    }

    @Override
    public void execute() {}

    @Override
    public void update(float dt) {
        delay -= dt;
        if(delay < 0) {
            if (!guideline.hasArrived()) {
                entity.setLocation(guideline.follow(Constants.ANIM_SPEED * dt));
                if (!tileLocation.equals(entity.getTileLocation())) {
                    tileLocation = entity.getTileLocation();
                }
            } else if(!finished) {
                finished = true;
                execute();
                detach();
                if(next != null) {
                    next.attach();
                }
            }
        }
    }
}