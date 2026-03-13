/**
 * Score Management Module
 * Handles saving, retrieving, and managing game scores
 * Communicates with /api/score backend endpoints
 */

class ScoreManager {
    constructor(backendUrl = "http://localhost:8585") {
        this.backendUrl = backendUrl;
        this.apiEndpoint = `${this.backendUrl}/api/score`;
    }

    /**
     * Save a score to the database
     * @param {string} personName - The player's name
     * @param {number} score - The score to save
     * @returns {Promise} - Response from the server
     */
    async saveScore(personName, score) {
        try {
            const response = await fetch(`${this.apiEndpoint}/save`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    personName: personName,
                    score: parseInt(score)
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            console.log("Score saved successfully:", data);
            return data;
        } catch (error) {
            console.error("Error saving score:", error);
            throw error;
        }
    }

    /**
     * Get all scores for a specific player
     * @param {string} personName - The player's name
     * @returns {Promise<Array>} - Array of scores for that player
     */
    async getPlayerScores(personName) {
        try {
            const response = await fetch(`${this.apiEndpoint}/person/${personName}`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            console.log(`Scores for ${personName}:`, data);
            return data;
        } catch (error) {
            console.error("Error fetching player scores:", error);
            throw error;
        }
    }

    /**
     * Get all scores in the database
     * @returns {Promise<Array>} - Array of all scores
     */
    async getAllScores() {
        try {
            const response = await fetch(`${this.apiEndpoint}/all`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            console.log("All scores:", data);
            return data;
        } catch (error) {
            console.error("Error fetching all scores:", error);
            throw error;
        }
    }

    /**
     * Get a score by ID
     * @param {number} scoreId - The score ID
     * @returns {Promise} - Score object
     */
    async getScoreById(scoreId) {
        try {
            const response = await fetch(`${this.apiEndpoint}/${scoreId}`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            console.log("Score found:", data);
            return data;
        } catch (error) {
            console.error("Error fetching score:", error);
            throw error;
        }
    }

    /**
     * Delete a score by ID
     * @param {number} scoreId - The score ID to delete
     * @returns {Promise} - Response message
     */
    async deleteScore(scoreId) {
        try {
            const response = await fetch(`${this.apiEndpoint}/${scoreId}`, {
                method: "DELETE"
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const message = await response.text();
            console.log("Score deleted:", message);
            return message;
        } catch (error) {
            console.error("Error deleting score:", error);
            throw error;
        }
    }

    /**
     * Display scores on the page (helper function)
     * @param {Array} scores - Array of score objects
     * @param {string} containerId - ID of the HTML element to display scores in
     */
    displayScores(scores, containerId = "scores-container") {
        const container = document.getElementById(containerId);
        if (!container) {
            console.error(`Container with ID '${containerId}' not found`);
            return;
        }

        if (scores.length === 0) {
            container.innerHTML = "<p>No scores found.</p>";
            return;
        }

        let html = "<table class='scores-table'><thead><tr><th>ID</th><th>Player</th><th>Score</th></tr></thead><tbody>";
        scores.forEach(score => {
            html += `<tr><td>${score.id}</td><td>${score.personName}</td><td>${score.score}</td></tr>`;
        });
        html += "</tbody></table>";

        container.innerHTML = html;
    }

    /**
     * Get the highest score from an array of scores
     * @param {Array} scores - Array of score objects
     * @returns {Object|null} - Highest score object or null if empty
     */
    getHighestScore(scores) {
        if (!scores || scores.length === 0) return null;
        return scores.reduce((max, score) => (score.score > max.score) ? score : max);
    }

    /**
     * Get the average score from an array of scores
     * @param {Array} scores - Array of score objects
     * @returns {number} - Average score
     */
    getAverageScore(scores) {
        if (!scores || scores.length === 0) return 0;
        const sum = scores.reduce((acc, score) => acc + score.score, 0);
        return (sum / scores.length).toFixed(2);
    }
}

// Create a global instance for easy access
const scoreManager = new ScoreManager();
