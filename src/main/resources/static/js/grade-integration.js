/**
 * Integration helper for submitting grades to the backend
 * Add this after loading your main grade collection script
 */

// Configuration - update based on your backend URL
const BACKEND_CONFIG = {
  baseUrl: 'http://localhost:8585',  // Updated to match your backend port
  apiPath: '/api/grades'
};

/**
 * Submit collected grades to the backend
 * @param {Object} gradeData - Grade data from CSPortfolioGrades.runGradeCollection()
 * @returns {Promise<Object>} Response from backend with student summaries
 */
async function submitGradesToBackend(gradeData) {
  try {
    console.log('Submitting grades to backend...');
    
    const response = await fetch(`${BACKEND_CONFIG.baseUrl}${BACKEND_CONFIG.apiPath}/submit`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(gradeData)
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const result = await response.json();
    
    console.log(' Grades successfully saved!');
    console.log(` Total grades saved: ${result.totalGradesSaved}`);
    console.log(` Total students: ${result.totalStudents}`);
    console.log(` Students: ${result.studentNames.join(', ')}`);
    
    // Display student summaries
    if (result.studentSummaries) {
      console.log('\n=== Student Summaries ===');
      for (const [name, summary] of Object.entries(result.studentSummaries)) {
        console.log(`\n${name}:`);
        console.log(`  Total Grades: ${summary.totalGrades}`);
        console.log(`  Average: ${summary.averageGrade}%`);
        console.log(`  Categories:`);
        summary.gradesByCategory.forEach(cat => {
          console.log(`    - ${cat.category}: ${cat.gradeCount} grades, avg ${cat.averageGrade}%`);
        });
      }
    }
    
    return result;
    
  } catch (error) {
    console.error(' Error submitting grades:', error);
    throw error;
  }
}

/**
 * Get all student names from backend
 * @returns {Promise<Array<string>>} List of student names
 */
async function getStudentList() {
  const response = await fetch(`${BACKEND_CONFIG.baseUrl}${BACKEND_CONFIG.apiPath}/students`);
  return await response.json();
}

/**
 * Get detailed summary for a specific student
 * @param {string} studentName - Name of the student
 * @returns {Promise<Object>} Student summary with averages and category breakdown
 */
async function getStudentSummary(studentName) {
  const encodedName = encodeURIComponent(studentName);
  const response = await fetch(`${BACKEND_CONFIG.baseUrl}${BACKEND_CONFIG.apiPath}/student/${encodedName}/summary`);
  
  if (!response.ok) {
    throw new Error(`Student not found: ${studentName}`);
  }
  
  return await response.json();
}

/**
 * Get all grades for a specific student
 * @param {string} studentName - Name of the student
 * @returns {Promise<Array>} List of all grades for the student
 */
async function getStudentGrades(studentName) {
  const encodedName = encodeURIComponent(studentName);
  const response = await fetch(`${BACKEND_CONFIG.baseUrl}${BACKEND_CONFIG.apiPath}/student/${encodedName}`);
  
  if (!response.ok) {
    throw new Error(`Student not found: ${studentName}`);
  }
  
  return await response.json();
}

/**
 * Get all grades for a specific category
 * @param {string} category - Category name (frontend, backend, etc.)
 * @returns {Promise<Array>} List of all grades in that category
 */
async function getGradesByCategory(category) {
  const response = await fetch(`${BACKEND_CONFIG.baseUrl}${BACKEND_CONFIG.apiPath}/category/${category}`);
  return await response.json();
}

/**
 * Get all grades organized by student name
 * @returns {Promise<Object>} Map of student names to their grades
 */
async function getAllGradesByStudent() {
  const response = await fetch(`${BACKEND_CONFIG.baseUrl}${BACKEND_CONFIG.apiPath}/by-student`);
  return await response.json();
}

/**
 * Complete workflow: Collect grades and submit to backend
 * @param {boolean} useMockData - Whether to use mock data
 * @returns {Promise<Object>} Backend response with student summaries
 */
async function collectAndSubmitGrades(useMockData = false) {
  console.log('🚀 Starting complete grade collection and submission workflow...\n');
  
  try {
    // Step 1: Collect grades using the JavaScript system
    console.log(' Step 1: Collecting grades from submodules...');
    const gradeData = await CSPortfolioGrades.runGradeCollection(useMockData);
    
    // Step 2: Submit to backend
    console.log('\n Step 2: Submitting grades to backend...');
    const result = await submitGradesToBackend(gradeData);
    
    console.log('\n Workflow complete!');
    return result;
    
  } catch (error) {
    console.error(' Workflow failed:', error);
    throw error;
  }
}

/**
 * Display student summary in a formatted way
 * @param {string} studentName - Name of the student
 */
async function displayStudentSummary(studentName) {
  try {
    const summary = await getStudentSummary(studentName);
    
    console.log(`\n=== Summary for ${summary.studentName} ===`);
    console.log(`Total Assignments: ${summary.totalGrades}`);
    console.log(`Overall Average: ${summary.averageGrade}%`);
    console.log('\nBreakdown by Category:');
    
    summary.gradesByCategory.forEach(cat => {
      console.log(`  ${cat.category.toUpperCase()}`);
      console.log(`    Assignments: ${cat.gradeCount}`);
      console.log(`    Average: ${cat.averageGrade}%`);
    });
    
    return summary;
  } catch (error) {
    console.error(`Error fetching summary for ${studentName}:`, error);
  }
}

/**
 * Check if backend is running
 * @returns {Promise<boolean>} True if backend is accessible
 */
async function checkBackendHealth() {
  try {
    const response = await fetch(`${BACKEND_CONFIG.baseUrl}${BACKEND_CONFIG.apiPath}/health`);
    if (response.ok) {
      const data = await response.json();
      console.log(' Backend is running:', data.message);
      return true;
    }
    return false;
  } catch (error) {
    console.error(' Backend is not accessible:', error.message);
    return false;
  }
}

// Add to global namespace for easy access
window.GradeBackendAPI = {
  // Core functions
  submitGrades: submitGradesToBackend,
  collectAndSubmit: collectAndSubmitGrades,
  
  // Query functions
  getStudentList,
  getStudentSummary,
  getStudentGrades,
  getGradesByCategory,
  getAllGradesByStudent,
  
  // Utility functions
  displayStudentSummary,
  checkBackendHealth,
  
  // Configuration
  config: BACKEND_CONFIG
};

// Console helper message
console.log(`
Grade Backend Integration Loaded!


`);

