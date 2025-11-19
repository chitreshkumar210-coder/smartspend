import { useRef, useState } from 'react';
import '../../../assets/styles/register.css';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import AuthService from '../../../services/auth.service';
import Logo from '../../../components/utils/Logo';

function Register() {

    const navigate = useNavigate();

    const {
        register,
        handleSubmit,
        watch,
        formState: { errors, isSubmitting }
    } = useForm({
        mode: 'onBlur'
    });
    const password = useRef({});
    password.current = watch('password', '');

    const [responseError, setResponseError] = useState('');

    const onSubmit = async (data) => {
        setResponseError('');
        try {
            const response = await AuthService.register_req(data.username, data.email, data.password);
            if (response.data.status === 'SUCCESS') {
                localStorage.setItem('message', JSON.stringify({ status: 'SUCCESS', text: 'Registration successful! Please login.' }));
                navigate('/auth/login');
            } else {
                setResponseError('Registration failed: Something went wrong!');
            }
        } catch (error) {
            if (error.response) {
                const resMessage = error.response.data.response;
                setResponseError(resMessage);
                console.log(error.response.data);
            } else {
                setResponseError('Registration failed: Something went wrong!');
            }
        }
    };

    return (
        <div className='container'>
            <form className='auth-form' noValidate onSubmit={handleSubmit(onSubmit)}>
                <Logo />
                <h2>Register</h2>
                {responseError !== '' && (
                    <div className='auth-form__status auth-form__status--error' role='alert'>
                        {responseError}
                    </div>
                )}
                <div className='input-box'>
                    <label htmlFor='register-username'>Username</label>
                    <input
                        type='text'
                        id='register-username'
                        autoComplete='username'
                        {...register('username', {
                            required: 'Username is required!'
                        })}
                        aria-invalid={errors.username ? 'true' : 'false'}
                        aria-describedby='register-username-error'
                    />
                    {errors.username && <small id='register-username-error'>{errors.username.message}</small>}
                </div>

                <div className='input-box'>
                    <label htmlFor='register-email'>Email</label>
                    <input
                        type='email'
                        id='register-email'
                        autoComplete='email'
                        {...register('email', {
                            required: 'Email is required!',
                            pattern: { value: /^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$/g, message: 'Invalid email address!' }
                        })}
                        aria-invalid={errors.email ? 'true' : 'false'}
                        aria-describedby='register-email-error'
                    />
                    {errors.email && <small id='register-email-error'>{errors.email.message}</small>}
                </div>

                <div className='input-box'>
                    <label htmlFor='register-password'>Password</label>
                    <input
                        type='password'
                        id='register-password'
                        autoComplete='new-password'
                        {...register('password', {
                            required: 'Password is required!',
                            minLength: {
                                value: 8,
                                message: 'Password must have atleast 8 characters'
                            }
                        })}
                        aria-invalid={errors.password ? 'true' : 'false'}
                        aria-describedby='register-password-error'
                    />
                    {errors.password && <small id='register-password-error'>{errors.password.message}</small>}
                </div>

                <div className='input-box'>
                    <label htmlFor='register-cpassword'>Confirm Password</label>
                    <input
                        type='password'
                        id='register-cpassword'
                        autoComplete='new-password'
                        {...register('cpassword', {
                            required: 'Confirm password is required!',
                            minLength: {
                                value: 8,
                                message: 'Password must have atleast 8 characters'
                            },
                            validate: cpass => cpass === password.current || 'Passwords do not match!'
                        })}
                        aria-invalid={errors.cpassword ? 'true' : 'false'}
                        aria-describedby='register-cpassword-error'
                    />
                    {errors.cpassword && <small id='register-cpassword-error'>{errors.cpassword.message}</small>}
                </div>

                <div className='input-box'>
                    <input
                        type='submit'
                        value={isSubmitting ? 'Please wait...' : 'Register'}
                        disabled={isSubmitting}
                        className={isSubmitting ? 'button button-fill loading' : 'button button-fill'}
                    />
                </div>
                <div className='fine-print'>By clicking Register, you agree to our user agreement, privacy policy, and cookie policy.</div>
                <div className='msg'>
                    Already a member?{' '}
                    <Link to='/auth/login' className='inline-link'>
                        Login Here
                    </Link>
                </div>
            </form>
        </div>
    );
}

export default Register;

