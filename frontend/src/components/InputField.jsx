export default function InputField({
  label,
  type = "text",
  name,
  placeholder,
  value,
  onChange,
  autoComplete
}) {
  return (
    <div className="flex flex-col w-full mb-3">
      <label className="text-sm font-medium mb-1">{label}</label>
      <input
        type={type}
        name={name}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        autoComplete={autoComplete}
        className="w-full border border-gray-300 rounded-md px-3 py-[10px] text-sm text-gray-800 
                   focus:outline-none focus:ring-2 focus:ring-black focus:border-black 
                   placeholder:text-gray-400 appearance-none"
      />
    </div>
  );
}
