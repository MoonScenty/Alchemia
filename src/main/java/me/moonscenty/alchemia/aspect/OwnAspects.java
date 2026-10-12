package me.moonscenty.alchemia.aspect;

/**
 * Something that carries aspects of its own on top of what its kind is made of -- a wisp of fire is fire as well as
 * a wisp, and a pech mage is a mage as well as a pech. The original read these off the creature itself.
 */
public interface OwnAspects {
    /** What this one adds to what every one of its kind has. */
    AspectList ownAspects();
}
