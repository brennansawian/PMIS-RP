document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('logout-link').addEventListener('click', async (event) => {
      event.preventDefault();

      console.log('Clicked');
      await fetch('/nerie/logout')
          .then(res => {
          if (res.ok)
            window.location.href = '/nerie/login';
          else
            alert('Error logging out.');
      })
          .catch(error => console.error(error.message));
  });
});