import { useEffect, useState } from 'react';
import '../../../assets/styles/register.css';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import AuthService from '../../../services/auth.service';
import Logo from '../../../components/utils/Logo';

function Login() {
    const navigate = useNavigate();

    useEffect(() => {
        const currentUser = AuthService.getCurrentUser();
        if (!currentUser) {
            return;
        }
        if (currentUser.roles.includes('ROLE_USER')) {
            navigate('/user/dashboard');
        } else if (currentUser.roles.includes('ROLE_ADMIN')) {
            navigate('/admin/transactions');
        }
    }, [navigate]);

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting }
    } = useForm({
        mode: 'onBlur'
    });

    const [responseError, setResponseError] = useState('');

    const onSubmit = async (data) => {
        setResponseError('');
        try {
            await AuthService.login_req(data.email, data.password);
            localStorage.setItem('message', JSON.stringify({ status: 'SUCCESS', text: 'Login successful!' }));

            setTimeout(() => {
                const currentUser = AuthService.getCurrentUser();
                if (!currentUser) return;
                if (currentUser.roles.includes('ROLE_USER')) {
                    navigate('/user/dashboard');
                } else if (currentUser.roles.includes('ROLE_ADMIN')) {
                    navigate('/admin/transactions');
                }
            }, 750);
        } catch (error) {
            const resMessage =
                (error.response && error.response.data && error.response.data.message) ||
                error.message ||
                error.toString();
            console.log(resMessage);
            if (resMessage === 'Bad credentials') {
                setResponseError('Invalid email or password.');
            } else {
                setResponseError('Something went wrong. Please try again later.');
            }
        }
    };

    return (
        <div className='container'>
            <form className='auth-form' noValidate onSubmit={handleSubmit(onSubmit)}>
                <Logo />
                <h2>Login</h2>
                {responseError !== '' && (
                    <div className='auth-form__status auth-form__status--error' role='alert'>
                        {responseError}
                    </div>
                )}

                <div className='input-box'>
                    <label htmlFor='login-email'>Email</label>
                    <input
                        type='email'
                        id='login-email'
                        autoComplete='email'
                        {...register('email', {
                            required: 'Email is required!',
                            pattern: { value: /^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$/g, message: 'Invalid email address!' }
                        })}
                        aria-invalid={errors.email ? 'true' : 'false'}
                        aria-describedby='login-email-error'
                    />
                    {errors.email && <small id='login-email-error'>{errors.email.message}</small>}
                </div>

                <div className='input-box'>
                    <label htmlFor='login-password'>Password</label>
                    <input
                        type='password'
                        id='login-password'
                        autoComplete='current-password'
                        {...register('password', {
                            required: 'Password is required!'
                        })}
                        aria-invalid={errors.password ? 'true' : 'false'}
                        aria-describedby='login-password-error'
                    />
                    {errors.password && <small id='login-password-error'>{errors.password.message}</small>}
                </div>

                <div className='msg'>
                    <Link to={'/auth/forgetpassword/verifyEmail'} className='inline-link'>
                        Forgot password?
                    </Link>
                </div>

                <div className='input-box'>
                    <input
                        type='submit'
                        value={isSubmitting ? 'Logging in...' : 'Login'}
                        disabled={isSubmitting}
                        className={isSubmitting ? 'button button-fill loading' : 'button button-fill'}
                    />
                </div>
                <div className='msg'>
                    New member?{' '}
                    <Link to='/auth/register' className='inline-link'>
                        Register Here
                    </Link>
                </div>
            </form>
        </div>
    );
}

export default Login;

