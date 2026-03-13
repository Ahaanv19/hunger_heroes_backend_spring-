# Student Grade Collection System

## Overview

This system receives JSON grade data from the frontend JavaScript grade collection system and stores it in a SQL database, organized by student name.

## Architecture

### Components

1. **StudentGrade.java** - JPA Entity representing a single grade entry
2. **StudentGradeRepository.java** - Repository interface for database operations
3. **GradeDataDTO.java** - Data Transfer Object matching the JavaScript JSON structure
4. **StudentGradesResponse.java** - Response DTO for returning organized data
5. **StudentGradeService.java** - Business logic for processing and organizing grades
6. **StudentGradeController.java** - REST API endpoints

## Database Schema

The `student_grades` table stores:
- `id` - Auto-generated primary key
- `category` - Category name (frontend, backend, data-viz, resume, ai, analytics)
- `submodule` - Submodule number
- `permalink` - URL path to the submodule
- `team` - Team name (Creators, Encrypters, Applicators, etc.)
- `status` - Status (success, mock, error, not_implemented)
- `student_name` - Student's full name
- `grade` - Numeric grade (0-100)
- `timestamp` - When the grade was recorded
- `note` - Additional notes
- `created_at` - When the record was created in the database

## API Endpoints

### POST /api/grades/submit
**Submit grade collection data from JavaScript**

**Request Body:**
```json
{
  "metadata": {
    "collectionDate": "2025-10-31T00:08:05.944Z",
    "totalSubmodules": 30,
    "baseUrl": "http://localhost:4500",
    "mode": "mock"
  },
  "summary": {
    "total": 30,
    "successful": 0,
    "mock": 30,
    "uniqueStudents": ["Taylor Jones", "Taylor Rodriguez", ...]
  },
  "allGrades": [
    {
      "category": "frontend",
      "submodule": 1,
      "permalink": "/cs-portfolio-quest/frontend/submodule_1/",
      "team": "Creators",
      "status": "mock",
      "studentName": "Taylor Jones",
      "grade": 69,
      "timestamp": "2025-10-31T00:08:05.944Z",
      "note": "Mock data generated for testing"
    },
    ...
  ]
}
```

**Response:**
```json
{
  "message": "Grades successfully saved and organized by student",
  "totalGradesSaved": 30,
  "totalStudents": 5,
  "studentNames": ["Casey Garcia", "Morgan Smith", "Quinn Jones", "Taylor Jones", "Taylor Rodriguez"],
  "studentSummaries": {
    "Taylor Jones": {
      "studentName": "Taylor Jones",
      "totalGrades": 10,
      "averageGrade": 80.5,
      "gradesByCategory": [
        {
          "category": "ai",
          "gradeCount": 1,
          "averageGrade": 80.0
        },
        ...
      ]
    }
  }
}
```

### GET /api/grades/students
**Get all student names**

**Response:**
```json
["Casey Garcia", "Morgan Smith", "Quinn Jones", "Taylor Jones", "Taylor Rodriguez"]
```

### GET /api/grades/student/{studentName}
**Get all grades for a specific student**

**Example:** `/api/grades/student/Taylor Jones`

**Response:**
```json
[
  {
    "id": 1,
    "category": "frontend",
    "submodule": 1,
    "permalink": "/cs-portfolio-quest/frontend/submodule_1/",
    "team": "Creators",
    "status": "mock",
    "studentName": "Taylor Jones",
    "grade": 69,
    "timestamp": "2025-10-31T00:08:05.944",
    "note": "Mock data generated for testing",
    "createdAt": "2025-10-31T12:34:56.789"
  },
  ...
]
```

### GET /api/grades/student/{studentName}/summary
**Get detailed summary for a specific student**

**Example:** `/api/grades/student/Taylor Jones/summary`

**Response:**
```json
{
  "studentName": "Taylor Jones",
  "totalGrades": 10,
  "averageGrade": 80.5,
  "gradesByCategory": [
    {
      "category": "ai",
      "gradeCount": 1,
      "averageGrade": 80.0
    },
    {
      "category": "backend",
      "gradeCount": 2,
      "averageGrade": 80.0
    },
    {
      "category": "frontend",
      "gradeCount": 4,
      "averageGrade": 77.25
    },
    {
      "category": "resume",
      "gradeCount": 1,
      "averageGrade": 98.0
    }
  ]
}
```

### GET /api/grades/category/{category}
**Get all grades for a specific category**

**Example:** `/api/grades/category/frontend`

### GET /api/grades/by-student
**Get all grades organized by student name**

**Response:**
```json
{
  "Taylor Jones": [
    { "id": 1, "category": "frontend", "grade": 69, ... },
    { "id": 2, "category": "frontend", "grade": 95, ... }
  ],
  "Taylor Rodriguez": [
    { "id": 4, "category": "frontend", "grade": 85, ... }
  ]
}
```

### GET /api/grades/student/{studentName}/average
**Get average grade for a student**

**Response:**
```json
{
  "studentName": "Taylor Jones",
  "averageGrade": 80.5
}
```

### GET /api/grades/student/{studentName}/category/{category}/average
**Get average grade for a student in a specific category**

**Example:** `/api/grades/student/Taylor Jones/category/frontend/average`

### GET /api/grades/health
**Health check endpoint**

## Integration with JavaScript

### Using Fetch API

```javascript
// After collecting grades with your JavaScript system
const grades = await CSPortfolioGrades.runGradeCollection();

// Submit to backend
const response = await fetch('http://localhost:8585/api/grades/submit', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify(grades)
});

const result = await response.json();
console.log('Grades saved:', result);
console.log('Students:', result.studentNames);
console.log('Summaries:', result.studentSummaries);
```

### Query Specific Student

```javascript
// Get all grades for Taylor Jones
const response = await fetch('http://localhost:8585/api/grades/student/Taylor Jones');
const studentGrades = await response.json();

// Get summary for Taylor Jones
const summaryResponse = await fetch('http://localhost:8585/api/grades/student/Taylor Jones/summary');
const summary = await summaryResponse.json();
console.log(`${summary.studentName} has ${summary.totalGrades} grades with average: ${summary.averageGrade}`);
```

## Features

1. **Automatic Data Organization**: Grades are automatically organized by student name upon submission
2. **Category Breakdown**: Each student's grades are grouped by category with averages
3. **Flexible Queries**: Multiple endpoints to query data by student, category, or combined
4. **Error Handling**: Graceful error handling with appropriate HTTP status codes
5. **CORS Enabled**: Cross-origin requests allowed for frontend integration
6. **Transactional**: Database operations are transactional to ensure data integrity

## Data Flow

1. JavaScript collects grades from all submodules
2. JavaScript sends complete JSON structure to `/api/grades/submit`
3. Backend validates and processes each grade entry
4. Grades are saved to SQLite database
5. Backend organizes data by student name
6. Response includes summary with averages and student lists
7. Frontend can query specific students or categories as needed

## Testing

### Test with cURL

```bash
# Submit test data
curl -X POST http://localhost:8585/api/grades/submit \
  -H "Content-Type: application/json" \
  -d @test-grades.json

# Get all students
curl http://localhost:8585/api/grades/students

# Get specific student summary
curl http://localhost:8585/api/grades/student/Taylor%20Jones/summary
```

### Test with Browser Console

```javascript
// In your browser console where the JavaScript is loaded
const grades = await CSPortfolioGrades.runGradeCollection(true); // Force mock data

// Submit to backend
const response = await fetch('http://localhost:8585/api/grades/submit', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(grades)
});

const result = await response.json();
console.table(result.studentNames);
```

## Database Queries

The repository provides several custom queries:

- Find by student name (ordered by timestamp)
- Find by category (ordered by student name)
- Find by student and category
- Get all unique student names
- Calculate average grades (overall and by category)

## Notes

- Student names must not be null to be saved
- Grades must be valid integers
- Timestamps are parsed from ISO format strings
- All data is stored in the `student_grades` table in SQLite
- The database file is located at `volumes/sqlite.db`

