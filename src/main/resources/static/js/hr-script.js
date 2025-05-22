document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM fully loaded and parsed');
    const sidebarLinks = document.querySelectorAll('.sidebar .nav-link:not(.dropdown-toggle)');
    const dropdownLinks = document.querySelectorAll('.dropdown-menu .dropdown-item[data-content]');
    const contentContainer = document.getElementById('content-container');

    if (!contentContainer) {
        console.error('Content container not found! Ensure #content-container exists in the DOM.');
        return;
    }

    const fetchContent = (contentType) => {
        console.log(`Fetching content for type: ${contentType}`);
        fetch(`/hr/content/${contentType}`, {
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'Accept': 'text/html'
            }
        })
        .then(response => {
            console.log(`Response status for ${contentType}: ${response.status}`);
            console.log(`Response Content-Type: ${response.headers.get('Content-Type')}`);
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(`Failed to fetch content: ${response.status} ${response.statusText}. Response: ${text}`);
                });
            }
            if (!response.headers.get('Content-Type').includes('text/html')) {
                throw new Error(`Invalid content type received: ${response.headers.get('Content-Type')}`);
            }
            return response.text();
        })
        .then(html => {
            console.log(`Content fetched for ${contentType}, HTML:`, html);
            try {
                contentContainer.innerHTML = html;
                console.log(`Content container updated for ${contentType}`);
            } catch (error) {
                console.error(`Error updating content container for ${contentType}:`, error);
                contentContainer.innerHTML = `
                    <div class="alert alert-danger">
                        Error rendering content for "${contentType}": ${error.message}
                    </div>`;
                throw error;
            }

            console.log(`Attaching handlers for ${contentType}`);
            try {
                attachFormHandler();
                attachActionButtons();
                attachSearchEmployeeHandler();
                attachFetchEmployeeHandler();
                attachDeleteEmployeeHandler();
                attachEditEmployeeHandler(); // Added to attach "Edit" button handlers
                attachDeleteEmployeeTableHandler(); // Added to attach "Delete" button handlers
                console.log(`Handlers attached successfully for ${contentType}`);
            } catch (error) {
                console.error(`Error attaching handlers for ${contentType}:`, error);
                contentContainer.innerHTML = `
                    <div class="alert alert-danger">
                        Error attaching handlers for "${contentType}": ${error.message}
                    </div>`;
                throw error;
            }

            switch (contentType) {
                case 'dashboard':
                    console.log('Initializing charts');
                    initializeCharts();
                    break;
                case 'calendar':
                    console.log('Loaded Calendar content');
                    break;
                case 'employees':
                    console.log('Loaded Employee Management content');
                    break;
                case 'recruitment':
                    console.log('Loaded Recruitment content');
                    break;
                case 'attendance':
                    console.log('Loaded Attendance content');
                    break;
                case 'departments':
                    console.log('Loaded Departments content');
                    break;
                case 'reports':
                    console.log('Loaded Reports content');
                    break;
                case 'company-policy':
                    console.log('Loaded Company Policy content');
                    break;
                case 'payroll-calculator':
                    console.log('Loaded Payroll Calculator content');
                    break;
                case 'add-employee':
                case 'update-employee':
                    console.log(`Loaded ${contentType === 'add-employee' ? 'Add Employee' : 'Update Employee'} form`);
                    setupImageUpload();
                    break;
                case 'search-employee':
                    console.log('Loaded Search Employee form');
                    break;
                case 'delete-employee':
                    console.log('Loaded Delete Employee form');
                    break;
                case 'employee-stats':
                    console.log('Loaded Employee Stats');
                    break;
                default:
                    console.warn(`No specific initialization for content type: ${contentType}`);
            }
        })
        .catch(error => {
            console.error(`Error fetching content for ${contentType}:`, error);
            contentContainer.innerHTML = `
                <div class="alert alert-danger">
                    Error loading content for "${contentType}": ${error.message}.
                    Please check if the server endpoint /hr/content/${contentType} is correctly set up.
                </div>`;
        });
    };

    const initializeCharts = () => {
        console.log('Initializing charts');
        const turnoverChartCanvas = document.getElementById('turnoverChart');
        const deptChartCanvas = document.getElementById('deptChart');
        if (turnoverChartCanvas) {
            console.log('Rendering turnover chart');
            new Chart(turnoverChartCanvas, {
                type: 'line',
                data: {
                    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
                    datasets: [{
                        label: 'Employee Turnover',
                        data: [12, 19, 3, 5, 2, 3],
                        borderColor: 'rgba(75, 192, 192, 1)',
                        fill: false
                    }]
                }
            });
        } else {
            console.warn('Turnover chart canvas not found');
        }
        if (deptChartCanvas) {
            console.log('Rendering department chart');
            new Chart(deptChartCanvas, {
                type: 'bar',
                data: {
                    labels: ['IT', 'HR', 'Finance', 'Marketing'],
                    datasets: [{
                        label: 'Employees per Department',
                        data: [30, 15, 10, 25],
                        backgroundColor: 'rgba(153, 102, 255, 0.2)',
                        borderColor: 'rgba(153, 102, 255, 1)',
                        borderWidth: 1
                    }]
                }
            });
        } else {
            console.warn('Department chart canvas not found');
        }
    };

    const attachFormHandler = () => {
        console.log('Attaching form handler');
        const forms = document.querySelectorAll('#add-employee-form, #update-employee-form');
        forms.forEach(form => {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                console.log('Form submission intercepted');
                const formData = new FormData(form);
                const contentType = form.id === 'add-employee-form' ? 'add-employee' : 'update-employee';
                console.log(`Submitting form for ${contentType}`);
                fetch(`/hr/content/${contentType}`, {
                    method: 'POST',
                    body: formData,
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                })
                .then(response => {
                    console.log(`Form submission response status: ${response.status}`);
                    if (!response.ok) {
                        return response.text().then(text => {
                            throw new Error(`Form submission failed: ${text}`);
                        });
                    }
                    return response.text();
                })
                .then(html => {
                    console.log(`Form submission successful, updating content`);
                    contentContainer.innerHTML = html;
                    attachFormHandler();
                })
                .catch(error => {
                    console.error(`Error submitting form for ${contentType}:`, error);
                    contentContainer.innerHTML = `
                        <div class="alert alert-danger">
                            Error submitting form: ${error.message}
                        </div>`;
                });
            });
        });
    };

    const attachLogoutHandler = () => {
            const logoutLink = document.getElementById('logoutLink');
            if (logoutLink) {
                logoutLink.addEventListener('click', (e) => {
                    e.preventDefault();
                    console.log('Logout link clicked, performing AJAX logout');
                    const headers = { 'X-Requested-With': 'XMLHttpRequest' };
                    if (csrfToken && csrfHeader) {
                        headers[csrfHeader] = csrfToken;
                    }
                    fetch('/logout', {
                        method: 'POST',
                        headers: headers,
                        credentials: 'include'
                    })
                    .then(response => {
                        if (!response.ok) throw new Error(`Logout failed: ${response.status}`);
                        console.log('Logout successful, redirecting to login page');
                        window.location.href = '/login?logout';
                    })
                    .catch(error => {
                        console.error('Error during logout:', error);
                        window.location.href = '/login?error=server';
                    });
                });
            } else {
                console.warn('Logout link not found - this is expected if not on a page with a logout link.');
            }
        };

    const attachActionButtons = () => {
        console.log('Attaching action buttons');
        document.querySelectorAll('.edit-employee').forEach(button => {
            button.addEventListener('click', () => {
                const employeeId = button.getAttribute('data-id');
                console.log(`Edit employee clicked for ID: ${employeeId}`);
                fetchContent(`update-employee/${employeeId}`);
            });
        });

        document.querySelectorAll('.delete-employee').forEach(button => {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                const employeeId = button.getAttribute('data-id') || document.getElementById('employee-id').value;
                console.log(`Delete employee clicked for ID: ${employeeId}`);
                if (confirm('Are you sure you want to delete this employee?')) {
                    fetch('/hr/employee/delete', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                            'X-Requested-With': 'XMLHttpRequest'
                        },
                        body: `employeeId=${employeeId}&_csrf=${document.querySelector('meta[name="_csrf"]').content}`
                    })
                    .then(response => {
                        console.log(`Delete response status: ${response.status}`);
                        if (response.ok) {
                            contentContainer.innerHTML = '<div class="alert alert-success">Employee deleted successfully</div>';
                            setTimeout(() => fetchContent('employees'), 2000);
                        } else {
                            return response.text().then(text => {
                                throw new Error(`Delete failed: ${text}`);
                            });
                        }
                    })
                    .catch(error => {
                        console.error('Error deleting employee:', error);
                        contentContainer.innerHTML = `<div class="alert alert-danger">Error: ${error.message}</div>`;
                    });
                }
            });
        });
    };

    const attachSearchEmployeeHandler = () => {
        console.log('Attaching search employee handler');
        const form = document.getElementById('search-employee-form');
        if (!form) {
            console.log('Search employee form not found');
            return;
        }

        const searchButton = form.querySelector('.btn-fetch');
        const searchInput = form.querySelector('#searchQuery');

        if (searchButton && searchInput) {
            console.log('Search employee button and input found');
            searchButton.addEventListener('click', () => {
                const query = searchInput.value.trim();
                if (query) {
                    console.log(`Searching for employee with query: ${query}`);
                    fetchContent(`search-employee?query=${encodeURIComponent(query)}`);
                } else {
                    console.log('Search query is empty');
                    fetchContent('search-employee'); // Reload the form without a query
                }
            });
        } else {
            console.log('Search employee button or input not found');
        }
    };

    // Handler for Edit buttons in the employee table
    const attachEditEmployeeHandler = () => {
        console.log('Attaching edit employee handler');
        const editButtons = document.querySelectorAll('#employee-table-body .btn-edit');
        editButtons.forEach(button => {
            button.addEventListener('click', () => {
                const employeeId = button.getAttribute('data-employee-id');
                console.log(`Fetching edit form for employee ID: ${employeeId}`);
                fetch(`/hr/content/update-employee/${employeeId}`, {
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Failed to fetch edit form: ${response.status} ${response.statusText}`);
                    }
                    return response.text();
                })
                .then(html => {
                    const contentContainer = document.getElementById('content-container');
                    if (contentContainer) {
                        contentContainer.innerHTML = html;
                        attachUpdateEmployeeHandler(); // Attach handler to the update form
                    }
                })
                .catch(error => {
                    console.error('Error fetching edit form:', error);
                    const contentContainer = document.getElementById('content-container');
                    if (contentContainer) {
                        contentContainer.innerHTML = `<div class="alert alert-danger">Error: ${error.message}</div>`;
                    }
                });
            });
        });
    };

    // Handler for Update Employee button in the update-employee form
    const attachUpdateEmployeeHandler = () => {
        console.log('Attaching update employee handler');
        const form = document.getElementById('update-employee-form');
        const formElement = document.getElementById('update-employee-form-element');
        if (!form || !formElement) {
            console.log('Update employee form not found');
            return;
        }

        const updateButton = form.querySelector('.btn-update-employee');
        const updateResult = document.getElementById('update-result');

        if (updateButton) {
            console.log('Update employee button found');
            updateButton.addEventListener('click', () => {
                const employeeId = updateButton.getAttribute('data-employee-id');
                const formData = new FormData(formElement);
                const qualifications = Array.from(formElement.querySelector('#qualifications').selectedOptions).map(option => option.value);
                formData.set('qualifications', qualifications); // Add qualifications as a comma-separated list
                const csrfToken = document.querySelector('meta[name="_csrf"]').content;
                formData.append('_csrf', csrfToken);

                console.log(`Updating employee with ID: ${employeeId}`);
                fetch('/hr/content/update-employee', {
                    method: 'POST',
                    body: formData
                })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Failed to update employee: ${response.status} ${response.statusText}`);
                    }
                    return response.text(); // Expect HTML response (employees-content fragment)
                })
                .then(html => {
                    console.log('Update successful, reloading employees-content');
                    const contentContainer = document.getElementById('content-container');
                    if (contentContainer) {
                        contentContainer.innerHTML = html;
                        attachEditEmployeeHandler(); // Re-attach handlers for the reloaded table
                        attachDeleteEmployeeTableHandler();
                    }
                })
                .catch(error => {
                    console.error('Error updating employee:', error);
                    if (updateResult) {
                        updateResult.className = 'alert alert-danger';
                        updateResult.innerHTML = `<i class="fas fa-exclamation-circle"></i> Error: ${error.message}`;
                    }
                });
            });
        } else {
            console.log('Update employee button not found');
        }
    };

    // Handler for Delete buttons in the employee table
    const attachDeleteEmployeeTableHandler = () => {
        console.log('Attaching delete employee table handler');
        const deleteButtons = document.querySelectorAll('#employee-table-body .btn-delete');
        deleteButtons.forEach(button => {
            button.addEventListener('click', () => {
                const employeeId = button.getAttribute('data-employee-id');
                const row = button.closest('tr');
                const csrfToken = document.querySelector('meta[name="_csrf"]').content;

                if (employeeId) {
                    if (confirm('Are you sure you want to delete this employee?')) {
                        console.log(`Deleting employee with ID: ${employeeId}`);
                        fetch('/hr/employee/delete', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/x-www-form-urlencoded',
                                'X-Requested-With': 'XMLHttpRequest'
                            },
                            body: `employeeId=${encodeURIComponent(employeeId)}&_csrf=${encodeURIComponent(csrfToken)}`
                        })
                        .then(response => {
                            if (!response.ok) {
                                throw new Error(`Failed to delete employee: ${response.status} ${response.statusText}`);
                            }
                            const contentType = response.headers.get('Content-Type');
                            if (contentType && contentType.includes('application/json')) {
                                return response.json();
                            } else {
                                throw new Error('Unexpected response format: Expected JSON');
                            }
                        })
                        .then(data => {
                            console.log('Delete response:', data);
                            const contentContainer = document.getElementById('content-container');
                            if (data.success) {
                                row.remove(); // Remove the row from the table
                                if (contentContainer) {
                                    const messageDiv = document.createElement('div');
                                    messageDiv.className = 'alert alert-success';
                                    messageDiv.innerHTML = `<i class="fas fa-check-circle"></i> ${data.message}`;
                                    contentContainer.insertBefore(messageDiv, contentContainer.firstChild);
                                }
                            } else {
                                if (contentContainer) {
                                    const errorDiv = document.createElement('div');
                                    errorDiv.className = 'alert alert-danger';
                                    errorDiv.innerHTML = `<i class="fas fa-exclamation-circle"></i> ${data.error}`;
                                    contentContainer.insertBefore(errorDiv, contentContainer.firstChild);
                                }
                            }
                        })
                        .catch(error => {
                            console.error('Error deleting employee:', error);
                            const contentContainer = document.getElementById('content-container');
                            if (contentContainer) {
                                const errorDiv = document.createElement('div');
                                errorDiv.className = 'alert alert-danger';
                                errorDiv.innerHTML = `<i class="fas fa-exclamation-circle"></i> Error: ${error.message}`;
                                contentContainer.insertBefore(errorDiv, contentContainer.firstChild);
                            }
                        });
                    }
                } else {
                    console.log('Employee ID not found for deletion');
                }
            });
        });
    };

    const attachFetchEmployeeHandler = () => {
        console.log('Attaching fetch employee handler for delete');
        const form = document.getElementById('delete-employee-form');
        if (!form) {
            console.log('Delete employee form not found');
            return;
        }

        const fetchButton = form.querySelector('.btn-fetch');
        const fetchInput = form.querySelector('#deleteQuery');

        if (fetchButton && fetchInput) {
            console.log('Fetch employee button and input found');
            fetchButton.addEventListener('click', () => {
                const query = fetchInput.value.trim();
                if (query) {
                    console.log(`Fetching employee with query: ${query}`);
                    fetchContent(`delete-employee?query=${encodeURIComponent(query)}`);
                } else {
                    console.log('Fetch query is empty');
                    fetchContent('delete-employee'); // Reload the form without a query
                }
            });
        } else {
            console.log('Fetch employee button or input not found');
        }
    };

    const attachDeleteEmployeeHandler = () => {
        console.log('Attaching delete employee handler');
        const form = document.getElementById('delete-employee-form');
        if (!form) {
            console.log('Delete employee form not found');
            return;
        }

        const deleteButton = form.querySelector('.btn-delete-employee');
        const employeeDetails = document.getElementById('employee-details');

        if (deleteButton) {
            console.log('Delete employee button found');
            deleteButton.addEventListener('click', () => {
                const employeeId = deleteButton.getAttribute('data-employee-id');
                const csrfToken = document.querySelector('meta[name="_csrf"]').content;

                if (employeeId) {
                    if (confirm('Are you sure you want to delete this employee?')) {
                        console.log(`Deleting employee with ID: ${employeeId}`);
                        fetch('/hr/employee/delete', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/x-www-form-urlencoded',
                                'X-Requested-With': 'XMLHttpRequest'
                            },
                            body: `employeeId=${encodeURIComponent(employeeId)}&_csrf=${encodeURIComponent(csrfToken)}`
                        })
                        .then(response => {
                            if (!response.ok) {
                                throw new Error(`Failed to delete employee: ${response.status} ${response.statusText}`);
                            }
                            const contentType = response.headers.get('Content-Type');
                            if (contentType && contentType.includes('application/json')) {
                                return response.json();
                            } else {
                                throw new Error('Unexpected response format: Expected JSON');
                            }
                        })
                        .then(data => {
                            console.log('Delete response:', data);
                            if (data.success) {
                                if (employeeDetails) {
                                    employeeDetails.innerHTML = `<div class="alert alert-success">${data.message}</div>`;
                                }
                            } else {
                                if (employeeDetails) {
                                    employeeDetails.innerHTML = `<div class="alert alert-danger">${data.error}</div>`;
                                }
                            }
                        })
                        .catch(error => {
                            console.error('Error deleting employee:', error);
                            if (employeeDetails) {
                                employeeDetails.innerHTML = `<div class="alert alert-danger">Error: ${error.message}</div>`;
                            }
                        });
                    }
                } else {
                    console.log('Employee ID not found for deletion');
                }
            });
        } else {
            console.log('Delete employee button not found');
        }
    };

    const setupImageUpload = () => {
        console.log('Setting up image upload');
        const profilePicInput = document.getElementById('profilePicInput');
        const profilePicPreview = document.getElementById('profilePicPreview');
        if (profilePicInput && profilePicPreview) {
            console.log('Profile picture input and preview found');
            profilePicInput.addEventListener('change', () => {
                const file = profilePicInput.files[0];
                console.log('Profile picture selected:', file ? file.name : 'none');
                if (file) {
                    const reader = new FileReader();
                    reader.onload = (e) => {
                        console.log('Image loaded for preview');
                        profilePicPreview.src = e.target.result;
                        profilePicPreview.style.display = 'block';
                    };
                    reader.readAsDataURL(file);
                }
            });
        } else {
            console.warn('Profile picture input or preview not found');
        }
    };

    sidebarLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const contentType = link.getAttribute('data-content');
            if (contentType) {
                console.log(`Sidebar link clicked: ${contentType}`);
                fetchContent(contentType);
                sidebarLinks.forEach(l => l.classList.remove('active'));
                link.classList.add('active');
            }
        });
    });

    dropdownLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const contentType = link.getAttribute('data-content');
            if (contentType) {
                console.log(`Dropdown item clicked: ${contentType}`);
                fetchContent(contentType);
                sidebarLinks.forEach(l => l.classList.remove('active'));
                const parentLink = document.querySelector('.sidebar .nav-link.dropdown-toggle');
                if (parentLink) parentLink.classList.add('active');
            }
        });
    });

    attachLogoutHandler();

    window.loadAddEmployeeForm = () => {
        console.log('loadAddEmployeeForm called');
        fetchContent('add-employee');
    };

    if (contentContainer.querySelector('#turnoverChart') || contentContainer.querySelector('#deptChart')) {
        initializeCharts();
    }

    const defaultLink = document.querySelector('.sidebar .nav-link[data-content="dashboard"]');
    if (defaultLink) {
        defaultLink.click();
    } else {
        console.warn('Default dashboard link not found');
    }
});