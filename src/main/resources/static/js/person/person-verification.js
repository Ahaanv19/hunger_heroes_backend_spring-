class Verification{
    
    static typeOfVerification = Object.freeze({
        none: -1,
        failed : 0,
        success : 1,
        email : 2,
    });

    uid;
    code;
    state = Verification.typeOfVerification.none;

    async attemptToVerifyWithOnlyUid(){
        let body = {
            uid: this.uid,
        }
        await fetch("/mvc/person/verification", {
                method: "POST",
                body: JSON.stringify(body),
                cache: "no-cache",
                headers: new Headers({
                    "content-type": "application/json"
                })
        }).then((response) => response.json()).then((data) => {
            if(!data.state){
                this.state = Verification.typeOfVerification.failed;
            } else {
                this.state = data.state;
            }
        })
    }

    async attemptToVerifyEmailWithCode(){
        let body = {
            uid: this.uid,
            code: this.code,
        }
        await fetch("/mvc/person/verification/code", {
                method: "POST",
                body: JSON.stringify(body),
                cache: "no-cache",
                headers: new Headers({
                    "content-type": "application/json"
                })
        }).then((response) => response.json()).then((data) => {
            if(!data.state){
                this.state = Verification.typeOfVerification.failed;
            } else {
                this.state = data.state;
            }
        })
    }

    setCode(code){
        this.code = code;
    }

    setUid(uid){
        this.uid = uid;
    }

    setState(state){
        this.state = state;
    }

    getCode(){
        return this.code;
    }

    getUid(){
        return this.uid;
    }

    getState(){
        return this.state;
    }
}
//other code starts here
let verification = new Verification();


//handle updating uid
function handleTyping(event){
    //update uid
    verification.setUid(document.getElementById("uid").value);
    //set state to unverified
    verification.setState(Verification.typeOfVerification.none);
}

//handle updating code (email verification)
function handleTyping2(event){
    //update uid
    verification.setCode(document.getElementById("code").value);
}

async function attemptVerify(){
    if(verification.getState() == Verification.typeOfVerification.none){
        await verification.attemptToVerifyWithOnlyUid();
        if(verification.getState() == Verification.typeOfVerification.success){
            // show other parts of person create and hide uid input feild
            document.getElementById("feild0").style.display = "none"; //hide uid
            document.getElementById("feild1").style.display = "block";
            document.getElementById("feild2").style.display = "block"; //show everything else
            document.getElementById("feild3").style.display = "block";
            document.getElementById("feild4").style.display = "block";
            document.getElementById("feild5").style.display = "block";
            document.getElementById("feild6").style.display = "block";
            document.getElementById("feild7").style.display = "block";
            document.getElementById("feild8").style.display = "block";

            document.getElementById("verifyButton").style.display = "none"; //hide verify button
        }
        else if(verification.getState() == Verification.typeOfVerification.email){
            //show code input feild, and hide uid input feild
            document.getElementById("feild0").style.display = "none";
            document.getElementById("feild0.5").style.display = "block";
        }
        else if(verification.getState() == Verification.typeOfVerification.failed){
            //something went wrong, display error message and try again
            verification.setState(Verification.typeOfVerification.none);

            alert("verification of account failed");
        }
    }
    else if(verification.getState() == Verification.typeOfVerification.email){
        await verification.attemptToVerifyEmailWithCode();
        if(verification.getState() == Verification.typeOfVerification.success){
            // show other parts of person create and hide uid input feild

            document.getElementById("email").value = document.getElementById("uid").value; //set email feild

            document.getElementById("feild0.5").style.display = "none"; //hide code
            document.getElementById("feild0").style.display = "none";
            document.getElementById("feild1").style.display = "block"; 
            document.getElementById("feild3").style.display = "block";
            document.getElementById("feild4").style.display = "block"; //show everything else
            document.getElementById("feild5").style.display = "block";
            document.getElementById("feild6").style.display = "block";
            document.getElementById("feild7").style.display = "block";
            document.getElementById("feild8").style.display = "block";

            document.getElementById("verifyButton").style.display = "none"; //hide verify button
        }
        else if(verification.getState() == Verification.typeOfVerification.failed){
            //something went wrong, display error message and try again
            verification.setState(Verification.typeOfVerification.email);

            alert("verification of email failed");
        }
    }
};

function validateForm(event) {
    // Check if the GitHub username is validated
    if(verification.getState() != Verification.typeOfVerification.success) {
        alert("You must validate your Account!");
        event.preventDefault();  // Prevent form submission if GitHub validation fails
        return;
    }

    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirm_password").value;
    
    // Check if the password and confirm password match
    if (password !== confirmPassword) {
        alert("Passwords do not match!");
        event.preventDefault();  // Prevent form submission if passwords don't match
        return;
    }
}