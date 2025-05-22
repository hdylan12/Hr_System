document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM fully loaded and parsed');
    const sidebarLinks = document.querySelectorAll('.sidebar .nav-link');
    const dropdownLinks = document.querySelectorAll('.dropdown-menu .dropdown-item[data-content]');
    const contentContainer = document.getElementById('content-container');

    console.log('Sidebar links found:', sidebarLinks.length);
    if (!contentContainer) {
        console.error('content-container element not found!');
        return;
    }

    // Get CSRF token and header
    const csrfTokenElement = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderElement = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken = csrfTokenElement ? csrfTokenElement.getAttribute('content') : null;
    const csrfHeader = csrfHeaderElement ? csrfHeaderElement.getAttribute('content') : null;

    if (!csrfToken || !csrfHeader) {
        console.warn('CSRF token or header not found. POST requests may fail.');
    }

    const fetchContent = (contentType) => {
        console.log(`Fetching content for type: ${contentType}`);
        contentContainer.innerHTML = '<div class="text-center"><i class="fas fa-spinner fa-spin fa-2x"></i> Loading...</div>';
        fetch(`/admin/content/${contentType}`, {
            method: 'GET',
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            credentials: 'include'
        })
        .then(response => {
            console.log(`Response status for ${contentType}: ${response.status}`);
            if (response.status === 401 || response.status === 403) {
                window.location.href = '/login?error=session';
                return;
            }
            if (!response.ok) {
                throw new Error(`Network response was not ok: ${response.statusText}`);
            }
            return response.text();
        })
        .then(html => {
            console.log(`Content fetched for ${contentType}, updating container`);
            contentContainer.innerHTML = html;
            // Reset z-index of content container and its parents
            contentContainer.style.zIndex = '1';
            contentContainer.parentElement.style.zIndex = '1';
            contentContainer.parentElement.parentElement.style.zIndex = '1';
            console.log('Z-index of content-container:', contentContainer.style.zIndex);
            console.log('Z-index of dashboard-content:', contentContainer.parentElement.style.zIndex);
            console.log('Z-index of main-content:', contentContainer.parentElement.parentElement.style.zIndex);
            attachFormHandler();
            attachLogoutHandler();
            if (contentType === 'add-user' || contentType === 'edit-user') {
                setupImageUpload();
            }
        })
        .catch(error => {
            console.error(`Error fetching content for ${contentType}:`, error);
            contentContainer.innerHTML = '<p>Error loading content. Please try again.</p>';
        });
    };

    const attachSidebarListeners = () => {
        const sidebarLinks = document.querySelectorAll('.sidebar .nav-link');
        console.log('Sidebar links found:', sidebarLinks.length);
        sidebarLinks.forEach(link => {
            link.removeEventListener('click', handleSidebarClick);
            link.addEventListener('click', handleSidebarClick);
        });

        const defaultLink = document.querySelector('.sidebar .nav-link[data-content="dashboard"]');
        if (defaultLink) {
            console.log('Default link found, triggering click');
            defaultLink.click();
        } else {
            console.error('Default link not found! Selector: .sidebar .nav-link[data-content="dashboard"]');
            sidebarLinks.forEach(link => {
                console.log('Sidebar link:', link.outerHTML);
            });
            console.log('Loading dashboard content as fallback');
            fetchContent('dashboard');
        }
    };

    const handleSidebarClick = (e) => {
        e.preventDefault();
        const link = e.currentTarget;
        const contentType = link.getAttribute('data-content');
        console.log('Sidebar link clicked:', contentType);
        if (contentType) {
            fetchContent(contentType);
            const sidebarLinks = document.querySelectorAll('.sidebar .nav-link');
            sidebarLinks.forEach(l => l.classList.remove('active'));
            link.classList.add('active');
        } else {
            console.error('No data-content attribute found on link:', link);
            // Fallback to href if no data-content
            const href = link.getAttribute('href');
            if (href) {
                console.log('Falling back to href:', href);
                window.location.href = href;
            }
        }
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

    const attachFormHandler = () => {
        const forms = contentContainer.querySelectorAll('form');
        forms.forEach(form => {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                const formData = new FormData(form);

                // Handle profile picture for user forms
                if (form.action.includes('/add-user') || form.action.includes('/edit-user')) {
                    const profilePicHidden = form.querySelector('#profilePicHidden');
                    if (profilePicHidden && !profilePicHidden.value) {
                        formData.delete('profilePicUpload');
                    }
                }

                console.log('Submitting form:', [...formData.entries()]);
                const headers = { 'X-Requested-With': 'XMLHttpRequest' };
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }
                fetch(form.action, {
                    method: form.method,
                    body: formData,
                    headers: headers,
                    credentials: 'include'
                })
                .then(response => {
                    if (response.status === 401 || response.status === 403) {
                        window.location.href = '/login?error=session';
                        return;
                    }
                    if (!response.ok) throw new Error(`Network response was not ok: ${response.status}`);
                    return response.text();
                })
                .then(html => {
                    contentContainer.innerHTML = html;
                    // Reset z-index after form submission
                    contentContainer.style.zIndex = '1';
                    contentContainer.parentElement.style.zIndex = '1';
                    contentContainer.parentElement.parentElement.style.zIndex = '1';
                    console.log('Z-index of content-container after form submission:', contentContainer.style.zIndex);
                    attachFormHandler();
                    if (form.action.includes('/add-user') || form.action.includes('/edit-user')) {
                        setupImageUpload();
                    }
                })
                .catch(error => {
                    console.error('Error submitting form:', error);
                    contentContainer.innerHTML = '<p>Error submitting form. Please try again.</p>';
                });
            });
        });
    };

    const setupImageUpload = () => {
        const fileInput = document.getElementById('profilePicUpload');
        const hiddenInput = document.getElementById('profilePicHidden');
        if (fileInput && hiddenInput) {
            fileInput.addEventListener('change', (e) => {
                const file = e.target.files[0];
                if (file) {
                    console.log('File selected:', file.name, 'Type:', file.type, 'Size:', file.size);
                    const validImageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/webp', 'image/heic', 'image/heif'];
                    const validExtension = /\.(jpe?g|png|gif|bmp|webp|heic|heif)$/i.test(file.name);
                    if (validImageTypes.includes(file.type) || (file.type === '' && validExtension)) {
                        const img = new Image();
                        img.src = URL.createObjectURL(file);
                        img.onload = () => {
                            const canvas = document.createElement('canvas');
                            const MAX_WIDTH = 800;
                            const MAX_HEIGHT = 800;
                            let width = img.width;
                            let height = img.height;

                            if (width > height) {
                                if (width > MAX_WIDTH) {
                                    height = Math.round((height * MAX_WIDTH) / width);
                                    width = MAX_WIDTH;
                                }
                            } else {
                                if (height > MAX_HEIGHT) {
                                    width = Math.round((width * MAX_HEIGHT) / height);
                                    height = MAX_HEIGHT;
                                }
                            }

                            canvas.width = width;
                            canvas.height = height;
                            const ctx = canvas.getContext('2d');
                            ctx.drawImage(img, 0, 0, width, height);

                            const base64String = canvas.toDataURL('image/jpeg', 0.7).split(',')[1];
                            hiddenInput.value = base64String;
                            console.log('Image resized and converted to Base64:', base64String.substring(0, 20) + '...');
                            URL.revokeObjectURL(img.src);
                        };
                        img.onerror = () => {
                            console.error('Error loading image:', file.name);
                            hiddenInput.value = '';
                        };
                    } else {
                        hiddenInput.value = '';
                        console.warn('Please upload a valid image file (JPEG, PNG, GIF, BMP, WebP, HEIC, HEIF). File type:', file.type);
                    }
                } else {
                    hiddenInput.value = '';
                    console.warn('No file selected.');
                }
            });
        } else {
            console.warn('File input or hidden input not found - this is expected if not on the add-user or edit-user form.');
        }
    };

    window.loadAddUserForm = () => {
        console.log('loadAddUserForm called');
        fetchContent('add-user');
    };

    dropdownLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const contentType = link.getAttribute('data-content');
            if (contentType) {
                fetchContent(contentType);
                const sidebarLinks = document.querySelectorAll('.sidebar .nav-link');
                sidebarLinks.forEach(l => l.classList.remove('active'));
                const matchingSidebarLink = document.querySelector(`.sidebar .nav-link[data-content="${contentType}"]`);
                if (matchingSidebarLink) matchingSidebarLink.classList.add('active');
            }
        });
    });

    attachSidebarListeners();
});