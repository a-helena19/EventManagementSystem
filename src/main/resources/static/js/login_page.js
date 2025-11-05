const container = document.getElementById('container');
const signUpButton = document.getElementById('register');
const signInButton = document.getElementById('login');





signUpButton.addEventListener('click', () => {

    container.classList.add("active");
});

signInButton.addEventListener('click', () => {

    container.classList.remove("active");
});

// soll theoretisch helfen?: noch keine notwendigkeit gefunden
// signUpButton.addEventListener('click', (e) => {
//     e.preventDefault();
//     container.classList.add("active");
// });
//
// signInButton.addEventListener('click', (e) => {
//     e.preventDefault();
//     container.classList.remove("active");
// });
