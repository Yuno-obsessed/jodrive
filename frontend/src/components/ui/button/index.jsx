import styles from "./button.module.css";
import clsx from "clsx";

const variants = {
  default: styles.primaryButton,
  submit: styles.submitButton,
  icon: styles.iconButton,
  destruction: styles.destructionButton,
  ghost: styles.ghostButton,
};

export const Button = ({
  title,
  children,
  className,
  ref,
  type,
  onClick,
  variant = "default",
}) => {
  return (
    <button
      title={title}
      ref={ref}
      type={type}
      onClick={onClick}
      className={clsx(className, variants[variant], variants["default"])}
    >
      {children}
    </button>
  );
};
