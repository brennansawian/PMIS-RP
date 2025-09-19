function handleAjaxError(jqXHR, textStatus, errorThrown) {
    switch (jqXHR.status) {
        case 400:
            Notiflix.Notify.Failure(`[400] BAD REQUEST: ${jqXHR.responseText}`)
            break
        case 401:
            Notiflix.Confirm.Ask(
                '[401] UNAUTHORIZED',
                'You are unauthenticated. Log in again?',
                '',
                'Yes',
                'No',
                () => {
                    window.location.href = '/nerie/login'
                }
            )
            break
        case 500:
            Notiflix.Notify.Failure(`[500] SERVER ERROR: Something went wrong`)
            break
        default:
            Notiflix.Notify.Failure("error:" + textStatus + " - exception:" + errorThrown)
    }
}

function handleFetchError(status, msg) {
    switch (status) {
        case 400:
            Notiflix.Notify.Failure(`[400] BAD REQUEST: ${msg}`)
            break
        case 401:
            Notiflix.Confirm.Ask(
                '[401] UNAUTHORIZED',
                'You are unauthenticated. Log in again?',
                '',
                'Yes',
                'No',
                () => {
                    window.location.href = '/nerie/login'
                }
            )
        case 500:
            Notiflix.Notify.Failure(`[500] SERVER ERROR: Something went wrong`)
            break

    } 
}

