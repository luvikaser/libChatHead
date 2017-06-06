package chathead.ChatHeadArrangement;

import android.os.Bundle;
import android.util.Log;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringChain;
import com.facebook.rebound.SpringListener;

import java.io.Serializable;
import java.util.List;

import chathead.ChatHeadManager.ChatHeadManager;
import chathead.ChatHeadUI.ChatHead;
import chathead.Utils.ChatHeadConfig;
import chathead.Utils.ChatHeadUtils;
import chathead.Utils.SpringConfigsHolder;

/**
 * Created by luvikaser on 07/03/2017.
 */

public class MinimizedArrangement<User extends Serializable> extends ChatHeadArrangement {
    public static final String BUNDLE_HERO_INDEX_KEY = "hero_index";
    public static final String BUNDLE_HERO_RELATIVE_X_KEY = "hero_relative_x";
    public static final String BUNDLE_HERO_RELATIVE_Y_KEY = "hero_relative_y";
    private Bundle extras;
    private static int MAX_VELOCITY_FOR_IDLING;
    private static int MIN_VELOCITY_TO_POSITION_BACK;
    private float DELTA = 0;
    private float currentDelta = 0;
    private int idleStateX = Integer.MIN_VALUE;
    private int idleStateY = Integer.MIN_VALUE;
    private int maxWidth;
    private int maxHeight;
    private boolean hasActivated = false;
    private ChatHeadManager manager;
    private SpringChain horizontalSpringChain;
    private SpringChain verticalSpringChain;
    private ChatHead hero;
    private double relativeXPosition = -1;
    private double relativeYPosition = -1;

    private SpringListener horizontalHeroListener = new SimpleSpringListener() {
        @Override
        public void onSpringUpdate(Spring spring) {
            currentDelta = (float) (DELTA * (maxWidth / 2 - spring.getCurrentValue()) / (maxWidth / 2));
            if (horizontalSpringChain != null)
                horizontalSpringChain.getControlSpring().setCurrentValue(spring.getCurrentValue());
        }

        @Override
        public void onSpringAtRest(Spring spring) {
            super.onSpringAtRest(spring);
        }
    };
    private SpringListener verticalHeroListener = new SimpleSpringListener() {
        @Override
        public void onSpringUpdate(Spring spring) {
            if (verticalSpringChain != null)
                verticalSpringChain.getControlSpring().setCurrentValue(spring.getCurrentValue());
        }

        @Override
        public void onSpringAtRest(Spring spring) {
            super.onSpringAtRest(spring);
        }
    };

    public MinimizedArrangement(ChatHeadManager manager) {
        this.manager = manager;
        DELTA = ChatHeadUtils.dpToPx(this.manager.getContext(), 5);
    }

    public void setIdleStateX(int idleStateX) {
        this.idleStateX = idleStateX;
    }

    public void setIdleStateY(int idleStateY) {
        this.idleStateY = idleStateY;
    }

    @Override
    public void onActivate(ChatHeadManager container, Bundle extras, int maxWidth, int maxHeight) {
        this.extras = extras;
        int heroIndex = 0;
        if (extras != null) {
            heroIndex = extras.getInt(BUNDLE_HERO_INDEX_KEY, -1);
            relativeXPosition = extras.getDouble(BUNDLE_HERO_RELATIVE_X_KEY, -1);
            relativeYPosition = extras.getDouble(BUNDLE_HERO_RELATIVE_Y_KEY, -1);
        }
        if (horizontalSpringChain != null || verticalSpringChain != null) {
            onDeactivate(maxWidth, maxHeight);
        }

        MIN_VELOCITY_TO_POSITION_BACK = ChatHeadUtils.dpToPx(container.getDisplayMetrics(), 600);
        MAX_VELOCITY_FOR_IDLING = ChatHeadUtils.dpToPx(container.getDisplayMetrics(), 1);

        List<ChatHead> chatHeads = container.getChatHeads();
        if (heroIndex < 0 || heroIndex > chatHeads.size() - 1)
            heroIndex = chatHeads.size() - 1;
        if (heroIndex < chatHeads.size() && heroIndex >= 0) {
            hero = chatHeads.get(heroIndex);
            hero.setHero(true);
            horizontalSpringChain = SpringChain.create();
            verticalSpringChain = SpringChain.create();
            for (int i = 0; i < chatHeads.size(); i++) {
                final ChatHead chatHead = chatHeads.get(i);
                if (chatHead != hero) {
                    chatHead.setHero(false);
                    horizontalSpringChain.addSpring(new SimpleSpringListener() {
                        @Override
                        public void onSpringUpdate(Spring spring) {
                            int index = horizontalSpringChain.getAllSprings().indexOf(spring);
                            int diff = index - horizontalSpringChain.getAllSprings().size() + 1;
                            chatHead.getHorizontalSpring().setCurrentValue(spring.getCurrentValue() + diff * currentDelta);
                        }
                    });
                    Spring currentSpring = horizontalSpringChain.getAllSprings().get(horizontalSpringChain.getAllSprings().size() - 1);
                    currentSpring.setCurrentValue(chatHead.getHorizontalSpring().getCurrentValue());
                    verticalSpringChain.addSpring(new SimpleSpringListener() {
                        @Override
                        public void onSpringUpdate(Spring spring) {
                            chatHead.getVerticalSpring().setCurrentValue(spring.getCurrentValue());
                        }
                    });
                    currentSpring = verticalSpringChain.getAllSprings().get(verticalSpringChain.getAllSprings().size() - 1);
                    currentSpring.setCurrentValue(chatHead.getVerticalSpring().getCurrentValue());
                    manager.getChatHeadContainer().bringToFront(chatHead);
                }
            }
            if (relativeXPosition == -1) {
                idleStateX = container.getConfig().getInitialPosition().x;
            } else {
                idleStateX = (int) (relativeXPosition * maxWidth);
            }
            if (relativeYPosition == -1) {
                idleStateY = container.getConfig().getInitialPosition().y;
            } else {
                idleStateY = (int) (relativeYPosition * maxHeight);
            }

            idleStateX = stickToEdgeX(idleStateX, maxWidth, hero);

            if (hero != null && hero.getHorizontalSpring() != null && hero.getVerticalSpring() != null) {
                manager.getChatHeadContainer().bringToFront(hero);
                horizontalSpringChain.addSpring(new SimpleSpringListener() {
                });
                verticalSpringChain.addSpring(new SimpleSpringListener() {
                });
                horizontalSpringChain.setControlSpringIndex(chatHeads.size() - 1);
                verticalSpringChain.setControlSpringIndex(chatHeads.size() - 1);

                hero.getHorizontalSpring().addListener(horizontalHeroListener);
                hero.getVerticalSpring().addListener(verticalHeroListener);

                hero.getHorizontalSpring().setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                hero.getHorizontalSpring().setEndValue(idleStateX);


                hero.getVerticalSpring().setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                hero.getVerticalSpring().setEndValue(idleStateY);
            }


            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
        }
        hasActivated = true;

    }

    private int stickToEdgeX(int currentX, int maxWidth, ChatHead chatHead) {
        if (maxWidth - currentX < currentX) {
            // this means right edge is closer
            return maxWidth - chatHead.getMeasuredWidth();
        } else {
            return 0;
        }
    }

    @Override
    public void onChatHeadAdded(ChatHead chatHead) {
        if (hero != null && hero.getHorizontalSpring() != null && hero.getVerticalSpring() != null) {
            chatHead.getHorizontalSpring().setCurrentValue(hero.getHorizontalSpring().getCurrentValue() - currentDelta);
            chatHead.getVerticalSpring().setCurrentValue(hero.getVerticalSpring().getCurrentValue());
        }

        onActivate(manager, getBundleWithHero(), maxWidth, maxHeight);
    }

    @Override
    public void onChatHeadRemoved(ChatHead removed) {
        if (removed == hero) {
            hero = null;
        }

        onActivate(manager, getBundleWithHero(), maxWidth, maxHeight);
    }
    @Override
    public Bundle getBundleWithHero() {
        if(hero != null) {
            relativeXPosition = hero.getHorizontalSpring().getCurrentValue() * 1.0 / maxWidth;
            relativeYPosition = hero.getVerticalSpring().getCurrentValue() * 1.0 / maxHeight;
        }

        Bundle bundle = extras;
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putInt(MaximizedArrangement.BUNDLE_HERO_INDEX_KEY, getHeroIndex());
        bundle.putDouble(MinimizedArrangement.BUNDLE_HERO_RELATIVE_X_KEY, relativeXPosition);
        bundle.putDouble(MinimizedArrangement.BUNDLE_HERO_RELATIVE_Y_KEY, relativeYPosition);
        return bundle;
    }

    @Override
    public void selectChatHead(ChatHead chatHead) {
        //manager.toggleArrangement();
    }

    @Override
    public void onDeactivate(int maxWidth, int maxHeight) {
        hasActivated = false;
        if (hero != null) {
            hero.getHorizontalSpring().removeListener(horizontalHeroListener);
            hero.getVerticalSpring().removeListener(verticalHeroListener);
        }
        if (horizontalSpringChain != null) {
            List<Spring> allSprings = horizontalSpringChain.getAllSprings();
            for (Spring spring : allSprings) {
                spring.destroy();
            }
        }
        if (verticalSpringChain != null) {
            List<Spring> allSprings = verticalSpringChain.getAllSprings();
            for (Spring spring : allSprings) {
                spring.destroy();
            }
        }

        horizontalSpringChain = null;
        verticalSpringChain = null;
    }

    @Override
    public boolean handleTouchUp(ChatHead activeChatHead, int xVelocity, int yVelocity, Spring
            activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {

        settleToClosest(activeChatHead, xVelocity, yVelocity);

        if (!wasDragging) {
            deactivate();
            return false;

        }
        return true;
    }

    private void settleToClosest(ChatHead activeChatHead, int xVelocity, int yVelocity) {
        Spring activeHorizontalSpring = activeChatHead.getHorizontalSpring();
        Spring activeVerticalSpring = activeChatHead.getVerticalSpring();
        if (activeChatHead.getState() == ChatHead.State.FREE) {
            if (Math.abs(xVelocity) < ChatHeadUtils.dpToPx(manager.getDisplayMetrics(), 50)) {
                if (activeHorizontalSpring.getCurrentValue() < (maxWidth - activeHorizontalSpring.getCurrentValue())) {
                    xVelocity = -1;
                } else {
                    xVelocity = 1;
                }
            }
            if (xVelocity < 0) {
                int newVelocity = (int) (-activeHorizontalSpring.getCurrentValue() * SpringConfigsHolder.DRAGGING.friction);
                if (xVelocity > newVelocity)
                    xVelocity = (newVelocity);

            } else if (xVelocity > 0) {
                int newVelocity = (int) ((maxWidth - activeHorizontalSpring.getCurrentValue() - manager.getConfig().getHeadWidth()) * SpringConfigsHolder.DRAGGING.friction);
                if (newVelocity > xVelocity)
                    xVelocity = (newVelocity);
            }


        }
        if (Math.abs(xVelocity) <= 1) {
            // this is a hack. If both velocities are 0, onSprintUpdate is not called and the chat head remains whereever it is
            // so we give a a negligible velocity to artificially fire onSpringUpdate
            if (xVelocity < 0)
                xVelocity = -1;
            else
                xVelocity = 1;
        }

        if (yVelocity == 0)
            yVelocity = 1;

        activeHorizontalSpring.setVelocity(xVelocity);
        activeVerticalSpring.setVelocity(yVelocity);
    }

    private void deactivate() {
        Bundle bundle = getBundleWithHero();
        manager.setArrangement(MaximizedArrangement.class, bundle);

    }
    /**
     * @return the index of the selected chat head a.k.a the hero
     */
    @Override
    public Integer getHeroIndex() {
        return getHeroIndex(hero);
    }

    private Integer getHeroIndex(ChatHead hero) {
        int heroIndex = 0;
        List<ChatHead> chatHeads = manager.getChatHeads();
        int i = 0;
        for (ChatHead chatHead : chatHeads) {
            if (hero == chatHead) {
                heroIndex = i;
            }
            i++;
        }
        return heroIndex;
    }

    @Override
    public void onConfigChanged(ChatHeadConfig newConfig) {

    }

    @Override
    public boolean canDrag(ChatHead chatHead) {
        return true; //all chat heads are draggable
    }

    @Override
    public void removeOldestChatHead() {
        manager.removeChatHead(manager.getChatHeads().get(0).getUser());
    }

    @Override
    public void onSpringUpdate(ChatHead activeChatHead, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity) {
        /** This method does a bounds Check **/
        if (!isDragging && Math.abs(totalVelocity) < MIN_VELOCITY_TO_POSITION_BACK && activeChatHead == hero) {

            if (Math.abs(totalVelocity) < MAX_VELOCITY_FOR_IDLING && activeChatHead.getState() == ChatHead.State.FREE && hasActivated) {
                setIdleStateX((int) activeHorizontalSpring.getCurrentValue());
                setIdleStateY((int) activeVerticalSpring.getCurrentValue());
            }
            if (spring == activeHorizontalSpring) {

                double xPosition = activeHorizontalSpring.getCurrentValue();
                if (xPosition + manager.getConfig().getHeadWidth() > maxWidth && activeHorizontalSpring.getVelocity() > 0) {
                    //outside the right bound
                    int newPos = maxWidth - manager.getConfig().getHeadWidth();
                    activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeHorizontalSpring.setEndValue(newPos);
                } else if (xPosition < 0 && activeHorizontalSpring.getVelocity() < 0) {
                    //outside the left bound
                    activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeHorizontalSpring.setEndValue(0);

                } else {
                    //within bound


                }
            } else if (spring == activeVerticalSpring) {
                double yPosition = activeVerticalSpring.getCurrentValue();
                if (yPosition + manager.getConfig().getHeadWidth() > maxHeight && activeVerticalSpring.getVelocity() > 0) {
                    //outside the bottom bound
                    //System.out.println("outside the bottom bound !! yPosition = " + yPosition);

                    activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeVerticalSpring.setEndValue(maxHeight - manager.getConfig().getHeadHeight());
                } else if (yPosition < 0 && activeVerticalSpring.getVelocity() < 0) {
                    //outside the top bound
                    //System.out.println("outside the top bound !! yPosition = " + yPosition);

                    activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeVerticalSpring.setEndValue(0);
                } else {
                    //within bound
                }

            }
        }

        if (!isDragging && activeChatHead == hero) {

            /** Capturing check **/


            double distanceCloseButtonFromHead = manager.getDistanceCloseButtonFromHead((float) activeHorizontalSpring.getCurrentValue() + manager.getConfig().getHeadWidth() / 2, (float) activeVerticalSpring.getCurrentValue() + manager.getConfig().getHeadHeight() / 2);

            if (distanceCloseButtonFromHead < activeChatHead.CLOSE_ATTRACTION_THRESHOLD && activeHorizontalSpring.getSpringConfig() == SpringConfigsHolder.DRAGGING && activeVerticalSpring.getSpringConfig() == SpringConfigsHolder.DRAGGING) {

                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);

                if (manager.getCloseButton().isDisappeared()) {
                    manager.getCloseButton().appear();
                    manager.getCloseButton().pointTo(manager.getCloseButton().centerX, manager.getCloseButton().centerY);
                }
                activeChatHead.setState(ChatHead.State.CAPTURED);

            }
            if (activeChatHead.getState() == ChatHead.State.CAPTURED && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.CAPTURING) {
                activeHorizontalSpring.setAtRest();
                activeVerticalSpring.setAtRest();
                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.CAPTURING);
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.CAPTURING);

            }
            if (activeChatHead.getState() == ChatHead.State.CAPTURED && activeVerticalSpring.isAtRest()) {
                manager.removeAllChatHeads();
                manager.getCloseButton().onCapture();
                manager.getCloseButton().disappear();
            }
        }


    }
    private Bundle getBundle(int heroIndex) {
        if(hero != null) {
            relativeXPosition = hero.getHorizontalSpring().getCurrentValue() * 1.0 / maxWidth;
            relativeYPosition = hero.getVerticalSpring().getCurrentValue() * 1.0 / maxHeight;
        }

        Bundle bundle = extras;
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putInt(MaximizedArrangement.BUNDLE_HERO_INDEX_KEY, heroIndex);
        bundle.putDouble(MinimizedArrangement.BUNDLE_HERO_RELATIVE_X_KEY, relativeXPosition);
        bundle.putDouble(MinimizedArrangement.BUNDLE_HERO_RELATIVE_Y_KEY, relativeYPosition);
        return bundle;
    }
    @Override
    public void bringToFront(ChatHead chatHead) {
        Bundle b = getBundle(getHeroIndex(chatHead));
        onActivate(manager, b, manager.getMaxWidth(), manager.getMaxHeight());

    }

}