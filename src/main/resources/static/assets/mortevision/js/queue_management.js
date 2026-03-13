document.querySelector(".navbar").style.zIndex = 4
let isSidebarClosed = true
let leftSidebar = document.getElementById("ManagementMenu")
let completedExpander = document.getElementById("expandableCompleted")
let isCompleteExpanded = false;
let completedStatus = document.getElementById("completedStatus")
function toggleCompletedExpander()
{
    isCompleteExpanded = !isCompleteExpanded;
    if(isCompleteExpanded)
    {
        completedStatus.innerText = "visibility"
        document.getElementById("doneList").style.display = "block"
        return
    }
    completedStatus.innerText = "visibility_off"
    document.getElementById("doneList").style.display = "none"
}


function toggleLeftSidebar() {
    isSidebarClosed = !isSidebarClosed;
    if (isSidebarClosed) {
        leftSidebar.style.transform = "translateX(-510px)"
        document.querySelectorAll(".directionalityFlip").forEach(el => el.style.transform = "rotate(180deg) translateX(-8px)")
        return
    }
    leftSidebar.style.transform = "translateX(0px)"
    document.querySelectorAll(".directionalityFlip").forEach(el => el.style.transform = "rotate(0deg) translateX(-8px)")
}

// --- Removed javaURI and socketURI definitions ---

// Dynamically construct WebSocket URI based on current protocol and host
const wsProtocol = window.location.protocol === "https:" ? "wss://" : "ws://";
const wsHost = window.location.host;
const socketURI = wsProtocol + wsHost + "/websocket";

let assignment = null;
let currentQueue = [];

document.getElementById('resetQueue').addEventListener('click', resetQueue);

let timerInterval;
let timerlength;
let queueUpdateInterval;

const URL = "/api/assignments/"

async function fetchQueue() {
    const response = await fetch(URL + `getQueue/${assignment}`);
    if (response.ok) {
        const data = await response.json();
        updateQueueDisplay(data);
    }
}

// add user to waiting
async function addToQueue() {
    await fetch(URL + `addToWaiting/${assignment}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify([person])
    });
    fetchQueue();
}

// remove user from waiting
async function removeFromQueue() {
    await fetch(URL + `removeToWorking/${assignment}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify([person])
    });
    fetchQueue();
}

// move user to completed
async function moveToDoneQueue() {
    const firstPerson = [currentQueue[0]];
    await fetch(URL + `doneToCompleted/${assignment}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(firstPerson)
    });
    fetchQueue();
}

// reset queue - todo: admin only
async function resetQueue() {
    await fetch(URL + `resetQueue/${assignment}`, {
        method: 'PUT'
    });
    fetchQueue();
}

// update display - ran periodically
function updateQueueDisplay(queue) {
    currentQueue = queue.waiting;

    const notGoneList = document.getElementById('notGoneList');
    const waitingList = document.getElementById('waitingList');
    const doneList = document.getElementById('doneList');

    notGoneList.innerHTML = queue.working.map(person => `<div class="card">${person}</div>`).join('');
    waitingList.innerHTML = queue.waiting.map(person => `<div class="card">${person}</div>`).join('');
    doneList.innerHTML = queue.completed.map(person => `<div class="card">${person}</div>`).join('');
    
}

document.getElementById('initializeQueue').addEventListener('click', initializeQueue);
document.getElementById('initializeIndividualQueue').addEventListener('click', initializeIndividualQueue);

// get assignments, used for initialization and popup connection
async function fetchAssignments() {
    console.log(URL + 'debug')
    const response = await fetch(URL + 'debug');
    if (response.ok) {
        const assignments = await response.json();
        const dropdown = document.getElementById('assignmentDropdown');
        dropdown.innerHTML = assignments.map(assignment =>
            `<option value="${assignment.id}">${assignment.name}</option>`
        ).join('');
    }
}

async function initializeQueue() {
    let peopleList;
    timerlength = document.getElementById("durationInput").value;
    const assignmentId = document.getElementById('assignmentDropdown').value;
    const checkedBoxes = [...document.querySelectorAll('#group-checkboxes input:checked')];
    const selectedGroupIds = checkedBoxes.map(cb => parseInt(cb.value));

    if (selectedGroupIds.length === 0) {
        alert("Please select at least one group.");
        return;
    }

    const response = await fetch('/api/groups');
    const allGroups = await response.json();

    const selectedGroups = allGroups.filter(group => selectedGroupIds.includes(group.id));

    const queueArray = selectedGroups.map(group =>
        `${group.name}: ${group.members.map(member => member.name).join(' | ')}`
    );

    peopleList = queueArray;

    await fetch(URL + `initQueue/${assignmentId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify([peopleList, [timerlength]])
    });
    assignment = assignmentId;
    fetchQueue();
}

async function initializeIndividualQueue() {
    let peopleList;
    timerlength = document.getElementById("durationInput").value;
    const assignmentId = document.getElementById('assignmentDropdown').value;
    const checkedBoxes = [...document.querySelectorAll('#group-checkboxes input:checked')];
    const selectedGroupIds = checkedBoxes.map(cb => parseInt(cb.value));

    if (selectedGroupIds.length === 0) {
        alert("Please select at least one group.");
        return;
    }

    const response = await fetch('/api/groups');
    const allGroups = await response.json();

    const selectedGroups = allGroups.filter(group => selectedGroupIds.includes(group.id));

    // Flatten the list of all members' names
    peopleList = selectedGroups.flatMap(group => group.members.map(member => member.name));

    console.log(selectedGroups);
    console.log(peopleList);

    await fetch(URL + `initQueue/${assignmentId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify([peopleList, [timerlength]])
    });

    assignment = assignmentId;
    fetchQueue();
}


// Start the interval to periodically update the queue
function startQueueUpdateInterval(intervalInSeconds) {
    if (queueUpdateInterval) clearInterval(queueUpdateInterval); // Clear existing interval if any
    queueUpdateInterval = setInterval(() => {
        console.log("Updating queue...");
        fetchQueue();
    }, intervalInSeconds * 1000);
}

// Stop the interval for queue updates if needed
function stopQueueUpdateInterval() {
    if (queueUpdateInterval) clearInterval(queueUpdateInterval);
}

window.addEventListener('load', () => {
    fetchUser();
    showAssignmentModal();
});

async function fetchUser() {
    const response = await fetch(`/api/person/get`, {
        method: 'GET',
        cache: "no-cache",
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            'X-Origin': 'client'
        }
    });
    if (response.ok) {
        const userInfo = await response.json();
        person = userInfo.name;
    }
}

function showAssignmentModal() {
    const modal = document.getElementById('assignmentModal');
    const modalDropdown = document.getElementById('modalAssignmentDropdown');

    // Fetch assignments and populate the dropdown
    fetchAssignments().then(() => {
        const dropdown = document.getElementById('assignmentDropdown');
        modalDropdown.innerHTML = dropdown.innerHTML; // Use the same data as the main dropdown
    });

    modal.style.display = 'block';

    // Add event listener for the confirm button
    document.getElementById('confirmAssignment').addEventListener('click', () => {
        let selectedAssignment = modalDropdown.value; // Use the actual assignment ID (like 58)
        console.log('Selected assignment ID:', selectedAssignment);
        
        // Get the selected option's text directly
        const selectedOptionText = modalDropdown.options[modalDropdown.selectedIndex].text;
        
        document.getElementById("viewingAssignmentTitle").innerText = `Viewing Assignment: ${selectedOptionText}`;
        
        assignment = selectedAssignment; // This will be the correct assignment ID (58, not 57)
        fetchQueue();
        startQueueUpdateInterval(10);
        modal.style.display = 'none';
    });
}

function loadGroups() {
    fetch('/api/groups')
        .then(response => response.json())
        .then(groups => {
            const container = document.getElementById('group-checkboxes');
            container.innerHTML = "<p>Select Groups:</p>";

            groups.forEach(group => {
                const label = document.createElement('label');
                label.innerHTML = `
                    <input type="checkbox" class="form-check-input" value="${group.id}" id="group-${group.id}"/>
                    <label class="form-check-label" for="group-${group.id}">
                        ${group.name}: ${group.members.map(m => m.name).join(', ')}
                    </label>
                `;
                container.appendChild(label);
                container.appendChild(document.createElement('br'));
            });
        })
        .catch(error => console.error("Error loading groups:", error));
}

document.addEventListener('DOMContentLoaded', loadGroups);

document.getElementById('groupSearch').addEventListener('input', function () {
    const search = this.value.toLowerCase();
    const groups = document.querySelectorAll('#group-checkboxes > label');

    groups.forEach(group => {
        const text = group.textContent.toLowerCase();
        group.style.display = text.includes(search) ? '' : 'none';
    });
});

const servers = {
    iceServers: [
        {
            urls: [
                "stun:stun.l.google.com:19302",
                "stun:stun.l.google.com:5349",
                "stun:stun1.l.google.com:3478",
                "stun:stun1.l.google.com:5349",
                "stun:stun2.l.google.com:19302",
                "stun:stun2.l.google.com:5349",
                "stun:stun3.l.google.com:3478",
                "stun:stun3.l.google.com:5349",
                "stun:stun4.l.google.com:19302",
                "stun:stun4.l.google.com:5349"
            ],
        },
    ],
    iceCandidatePoolSize: 10,
};

const socket = new WebSocket(socketURI);
let videoStreamGlobal;
let globalPeer;

socket.onmessage = async function (event) {
    const messageData = JSON.parse(event.data);
    switch (messageData["context"]) {
        case "broadcastRequestServer":
            await watch()
            break;
        case "viewerOfferServer":
            viewerOfferServer(messageData);
            break;
        case "viewerAcceptServer":
            viewerAcceptServer(messageData);
            break;
        case "iceToStreamerServer":
        case "iceToViewerServer":
            globalPeer.addIceCandidate(new RTCIceCandidate(JSON.parse(messageData["candidate"])));
            break;
    }
};

function sendMessage(message) {
    if (socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify(message));
    } else {
        console.error("WebSocket connection is not open.");
    }
}

async function viewerOfferServer(messageData) {
    const peer = new RTCPeerConnection(servers);
    globalPeer = peer;

    let remotedesc = new RTCSessionDescription({
        type: "offer",
        sdp: messageData["sdp"]
    });

    peer.onicecandidate = (e) => {
        if (e.candidate) {
            sendMessage({ context: "iceToViewerClient", candidate: JSON.stringify(e.candidate.toJSON()) });
        }
    };

    await peer.setRemoteDescription(remotedesc);
    videoStreamGlobal.getTracks().forEach(track => peer.addTrack(track, videoStreamGlobal));
    const answer = await peer.createAnswer();
    await peer.setLocalDescription(answer);

    sendMessage({
        context: "viewerAcceptClient",
        sdp: answer.sdp,
        returnID: messageData["returnID"]
    });
}

function viewerAcceptServer(messageData) {
    let remotedesc = new RTCSessionDescription({
        type: "answer",
        sdp: messageData["sdp"]
    });

    if (globalPeer.signalingState === "stable") {
        console.warn("Skipping setRemoteDescription because connection is already stable.");
        return;
    }

    globalPeer.setRemoteDescription(remotedesc)
        .then(() => {
            console.log("Remote description set successfully.");
        })
        .catch(error => {
            console.error("Failed to set remote description:", error);
        });

    globalPeer.ontrack = (event) => {
        document.getElementById("mortStream").srcObject = event.streams[0];
        document.getElementById("mortStream").style.display = "block";
        document.getElementById("streamOffline").style.display = "none";
    };
}

async function watch() {
    const peer = new RTCPeerConnection(servers);
    peer.addTransceiver("video", { direction: "recvonly" });
    const offer = await peer.createOffer();
    await peer.setLocalDescription(offer);

    peer.onicecandidate = (e) => {
        if (e.candidate) {
            sendMessage({ context: "iceToStreamerClient", candidate: JSON.stringify(e.candidate.toJSON()) });
        }
    };

    globalPeer = peer;
    sendMessage({ context: "viewerOfferClient", sdp: offer.sdp });
}

socket.onerror = function (error) {
    console.error("WebSocket error: ", error);
};

socket.onclose = function (event) {
    console.log("WebSocket connection closed:", event);
};

socket.onopen = function (event) {
    console.log("WebSocket connection established.");
};