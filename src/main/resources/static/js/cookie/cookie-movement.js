
var gravity = 6;

var positionX = 50; //50 == 50%
var positionY = 50;

var velocityX = 0;
var velocityY = 0;

var rotation = 0;
var rotationVelocity = 0;

var canvas = document.getElementById("cookie");

function updatePositionToVelocity() {
    velocityY = Math.min(velocityY, 10);
    velocityY = Math.max(velocityY, -10);
    velocityX = Math.min(velocityX, 5);
    velocityX = Math.max(velocityX, -5);
    // 0,0 is at top left corner and I want it to be bottom left
    positionX = positionX + velocityX;
    positionY = positionY + velocityY; //y is opposite;

    positionX = Math.max(positionX, -40);
    positionY = Math.max(positionY, -40);
}
function updateStyle() {
    var deltaX = 50 - positionX;
    var deltaY = 50 - positionY;

    deltaX = Math.max(deltaX, -40);
    deltaX = Math.min(deltaX, 40) * 10;
    deltaY = Math.max(deltaY, -40);
    deltaY = Math.min(deltaY, 40) * 10;

    if (deltaY == -400) {
        velocityY = Math.min(velocityY, 0);
        //fix the infinite height glitch
        positionY = Math.min(positionY,100);
    }
    if (deltaX == -1000) {
        velocityX = Math.min(velocityX, 0);
    }

    // 0,0 is at top left corner and I want it to be bottom left
    var left = deltaX.toString() + "%";
    var top = deltaY.toString() + "%";
    var rot = rotation.toString() + "deg"; // sting
    canvas.style.transform = "translate(" + left + "," + top + ") rotate(" + rot + ")";
}

var fps = 24;
var active = true;
var animId;
var currentFrame = 0;
function frame() {
    currentFrame = (currentFrame + 1) % fps;

    velocityY -= gravity / fps;
    rotationVelocity *= 1 - .9 / fps;
    if (Math.abs(rotationVelocity) < 3) {
        rotationVelocity = 0;
        velocityX = 0;
    }
    rotation += rotationVelocity;
    updatePositionToVelocity();
    updateStyle();

    // Continue the animation loop
    setTimeout(function () {
        if (active == true) {
            animId = requestAnimationFrame(frame);
        }
    }, 1000 / fps);
}
var directionMult = .5;
canvas.addEventListener("click", () => {
    rotationVelocity = Math.random() * 120 * directionMult;
    if (directionMult >= 0) { directionMult = -.5 } else { directionMult = .5 };
    velocityX = -rotationVelocity / 12;
    velocityY = 8;
    updatePositionToVelocity();
    updateStyle();

    var selectors = $("p,li,h1,h2,h3,h4").not('.gradient').not('#counter');
    if (selectors.length > 0) {
        selectors.eq(Math.floor(Math.random() * selectors.length)).addClass("gradient");
    }
    else {
        selectors = $("*:visible").not('.gradientB').not('.gradient').not('#counter');
        selectors.eq(Math.floor(Math.random() * selectors.length)).addClass("gradientB");
    }

})

// Start the animation loop
frame();