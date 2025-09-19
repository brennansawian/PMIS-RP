document.addEventListener('DOMContentLoaded', () => {
    const feedbackButtons = document.querySelectorAll('.feedback-btn');

    feedbackButtons.forEach(button => {
        const subcode = button.dataset.scode
        const fcode = button.dataset.fcode

        button.addEventListener('click', () => {
            document.getElementById('feedbacksubjectcode').value = subcode
            document.getElementById('feedbackfacultyid').value = fcode
        })
    })

    document.getElementById('studentfeedbackform').addEventListener('submit', e => handleSubmit(e))
})

// TODO @Abanggi: Refactor this terrible code
const handleSubmit = (e) => {
    e.preventDefault()

    let subjectcode = document.getElementById('feedbacksubjectcode').value
    let studentid = document.getElementById('feedbackstudentid').value
    let facultyid = document.getElementById('feedbackfacultyid').value
    let feedback = document.getElementById('formfeedbackinput').value
    let entrydate = new Date().toISOString()
    const payload = JSON.stringify({ feedback, subjectcode, studentid, facultyid, entrydate })

    fetch('/nerie/feedbacks/postsubjectfeedback', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: payload
    })
    .then(res => {
        if (res.ok) {
            alert('Feedback submitted successfully!')
            window.location.reload()
        }
    })
    .catch(err => {
        alert(err.message)
        window.location.reload()
    })
}
