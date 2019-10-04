package actions;

import app.GameHandler;

public abstract class Action {
    Action next;
    float delay;
    boolean finished = false, attached = false;
    protected Object[] args;

    public Action(float delay, Object... args) {
        this.args = args;
        this.delay = delay;
    }

    public Action() {
        this(0);
    }

    public Action insertAction(Action a) {
        Action temp = next;
        next = a;
        a.addAction(temp);
        return this;
    }

    public Action addAction(Action _next) {
        if(next != null)
            next.addAction(_next);
        else
            this.next = _next;
        return this;
    }

    public void attach() {
        if(!attached) {
            attached = true;
            GameHandler.actions.add(this);
        }
    }

    protected void detach() {
        if(attached)
        	GameHandler.actions.remove(this);
    }

    public void setDelay(float delay) {
        this.delay = delay;
    }

    protected abstract void execute();

    public void executeNow() {
    	detach();
    	execute();
    	if(next!=null)
    		next.executeNow();
    }
    
    public void update(float dt) {
        delay -= dt;
        if(delay > 0 || finished) return;
        finished = true;
        execute();

        detach();
        if(next != null) {
            next.attach();
        }
    }

    public static Action empty() {
        return new Action(0) {
            @Override
            public void execute() {}
        };
    }
}
