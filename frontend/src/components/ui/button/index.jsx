import styles from "./button.module.css";
import clsx from "clsx";

const variants = {
  default: styles.primaryButton,
  submit: styles.submitButton,
  icon: styles.iconButton,
  destruction: styles.destructionButton,
};

export const Button = ({
  children,
  className,
  type,
  onClick,
  variant = "default",
}) => {
  return (
    <button
      type={type}
      onClick={onClick}
      className={clsx(className, variants[variant], variants["default"])}
    >
      {children}
    </button>
  );
};
