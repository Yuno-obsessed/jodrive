import styles from "./input.module.css";
import clsx from "clsx";

const variants = {
  primary: styles.primaryInput,
};

export const Input = ({
  value,
  label,
  name,
  action,
  placeholder,
  className,
  type,
  onChange,
  variant = "primary",
}) => (
  <div className={clsx(styles.inputWrapper, className)}>
    {label && <label htmlFor="input-field">{label}</label>}
    <input
      type={type}
      value={value}
      name={name}
      className={variants[variant]}
      placeholder={placeholder}
      onChange={onChange}
    />
    {action}
  </div>
);
