export default function Ornamento({ className = '' }) {
  return (
    <svg className={`ornamento ${className}`} viewBox="0 0 220 16" aria-hidden="true" focusable="false">
      <line x1="0" y1="8" x2="92" y2="8" />
      <line x1="128" y1="8" x2="220" y2="8" />
      <path d="M110 1 L117 8 L110 15 L103 8 Z" />
      <circle cx="96" cy="8" r="1.6" />
      <circle cx="124" cy="8" r="1.6" />
    </svg>
  )
}
