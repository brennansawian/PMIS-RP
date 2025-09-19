const logout = async () => {
    await fetch('/nerie/logout').then(res => {
        if (res.ok)
            window.location.href = '/nerie/login'
        else
            alert('Error logging out.')
    }).catch(error => console.err(error.message))
}