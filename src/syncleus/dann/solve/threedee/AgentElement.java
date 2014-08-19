package syncleus.dann.solve.threedee;

//package org.simbrain.world.threedee;
//
//import java.util.List;
//
//import com.jme.bounding.BoundingBox;
//import com.jme.math.Vector3f;
//import com.jme.renderer.ColorRGBA;
//import com.jme.renderer.Renderer;
//import com.jme.scene.Node;
//import com.jme.scene.shape.Box;
//
///**
// * Wraps an Agent and gives it a visible 'body' by extending
// * MultipleViewElement.
// *
// * @author Matt Watson
// */
//public class AgentElement extends MultipleViewElement<Node> {
//    /** The up axis. */
//    private static final Vector3f Y_AXIS = new Vector3f(0f, 1f, 0f);
//
//    /** The agent this element wraps. */
//    private final Agent agent;
//
//    /**
//     * Creates an new instance for the given agent.
//     *
//     * @param agent the agent this object will wrap.
//     */
//    public AgentElement(final Agent agent) {
//        this.agent = agent;
//    }
//
//    public Agent getAgent() {
//        return agent;
//    }
//
//    /**
//     * Initializes one spatial node.
//     *
//     * @param renderer the renderer to initialize with.
//     * @param node the node to initialize.
//     */
//    @Override
//    public void initSpatial(final Renderer renderer, final Node node) {
//        node.setLocalTranslation(agent.getLocation().toVector3f());
//    }
//
//    /**
//     * Creates a node for this agent.
//     *
//     * @return the node that is created.
//     */
//    @Override
//    public Node create() {
//        final Box b = new Box("box", new Vector3f(), 0.35f, 0.25f, 0.5f);
//        b.setModelBound(new BoundingBox());
//        b.updateModelBound();
//        b.setDefaultColor(ColorRGBA.red);
//        final Node node = new Node("Player Node");
//        node.attachChild(b);
//        node.setModelBound(new BoundingBox());
//        node.updateModelBound();
//        return node;
//    }
//
//    /**
//     * Updates one node based on the agent.
//     *
//     * @param node the node to update.
//     */
//    @Override
//    public void updateSpatial(final Node node) {
//
//        if (agent.getLocation() == null) {
//            return;
//        }
//
//        node.lookAt(agent.getLocation().add(agent.getDirection()).toVector3f(),
//                Y_AXIS);
//        node.setLocalTranslation(agent.getLocation().toVector3f());
//    }
//
//    /**
//     * Calls agent.collision.
//     *
//     * @param collision the collision data.
//     */
//    public void collision(final Collision collision) {
//        agent.collision(collision);
//    }
//
//    /**
//     * Calls agent.getTenative.
//     *
//     * @return the tentative spatial data.
//     */
//    public SpatialData getTentative() {
//        return agent.getTentative();
//    }
//
//    /**
//     * Calls agent.commit.
//     */
//    public void commit() {
//        agent.commit();
//    }
//
//    public void setFloor(float height) {
//        agent.setFloor(height);
//    }
//
//    public Point getLocation() {
//        return agent.getLocation();
//    }
//
//    public void setTentativeLocation(Point point) {
//        agent.setTentativeLocation(point);
//    }
//
//    public List<Odor> getOdors() {
//        return agent.getOdors();
//    }
//}
