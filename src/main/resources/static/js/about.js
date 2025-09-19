document.addEventListener('DOMContentLoaded', () => {
    const buttons = document.querySelectorAll('.windowOpenBtn')

    buttons.forEach(button => {
      let fileName = button.getAttribute('data-arg')

      button.addEventListener('click', () => window.open(fileName, 'mywindow'))
    })
})