async function getCompany() {
    let dataOut;
    await fetch("/api/train/get/company", {
        method: "GET",
        cache: "no-cache",
    }).then((response) => response.json()).then((data) => {
        dataOut = data;
    })
    return dataOut;
}

async function attemptBuyTrain(train) {
    let dataOut;
    await fetch("/api/train/buy/train", {
        method: "POST",
        cache: "no-cache",
        body: JSON.stringify(train),
        headers: new Headers({
            "content-type": "application/json"
        })
    }).then((response) => response.json()).then((data) => {
        dataOut = data;
    })
    return dataOut;
}

function fillCompanyHtml(container,companyInfo){
    console.log(companyInfo);
    let h1 = document.createElement("h1");
    h1.innerText = companyInfo.companyName;

    let div = document.createElement("div");
    let a = document.createElement("a");
    a.innerText = "owner";
    a.setAttribute("href","/mvc/person/read/"+companyInfo.id);
    a.setAttribute("class","btn btn-info btn-sm")

    div.append(a);

    let div2 = document.createElement("div");
    div2.setAttribute("class","form-group mx-sm-3 mb-2")
    let h2 = document.createElement("h2");
    h2.innerText = "Balance: ";
    let span = document.createElement("span");
    span.setAttribute("class","badge");
    span.innerText = String(companyInfo.balance);
    h2.append(span);
    div2.append(h2);

    container.append(h1);
    container.append(div);
    container.append(document.createElement("hr"));
    container.append(div2);
}

function getTrainImage(trainInfo){
    let image = new Image;
    image.src = "/images/trains/"+String(trainInfo.trainType)+"_"+String(trainInfo.trainColor)+".png";
    //image.width = 128;
    //image.height = 16;
    return image;
}

function buildCard(trainType,trainColor){
    let div = document.createElement("div");
    div.setAttribute("class","card m-auto");
    div.style.width= "30%";
    let image = getTrainImage({"trainType":trainType,"trainColor":trainColor});
    image.setAttribute("class","card-img-top");
    let div2 = document.createElement("div");
    div2.setAttribute("class","card-body");
    let h5 = document.createElement("h5");
    h5.setAttribute("class","card-title");
    h5.innerText = String(trainColor)+" "+String(trainType);
    let p = document.createElement("p");
    p.setAttribute("class","card-text");
    p.innerText = "cost: "+String(0); //default price of 5000
    let button = document.createElement("button");
    button.setAttribute("class","btn btn-secondary btn-sm");
    button.innerText = "buy";
    button.addEventListener("click", async ()=>{
        let content = await attemptBuyTrain({"trainType":trainType,"trainColor":trainColor});
        console.log(content);
    })

    div2.append(h5);
    div2.append(p);
    div2.append(button);

    div.append(image);
    div.append(div2);

    return div;
}

function fillStoreHtml(container){
    let redTrolleyCard = buildCard("trolley","red");
    let orangeTrolleyCard = buildCard("trolley","orange");
    let yellowTrolleyCard = buildCard("trolley","yellow");
    let greenTrolleyCard = buildCard("trolley","green");
    let blueTrolleyCard = buildCard("trolley","blue");
    let purpleTrolleyCard = buildCard("trolley","purple");
    container.append(redTrolleyCard);
    container.append(orangeTrolleyCard);
    container.append(yellowTrolleyCard);
    container.append(greenTrolleyCard);
    container.append(blueTrolleyCard);
    container.append(purpleTrolleyCard);
}

async function setUp(){
    let company = await getCompany();
    fillCompanyHtml(document.getElementById("companyContainer"),company);
    fillStoreHtml(document.getElementById("storeContainer"));
}

setUp();