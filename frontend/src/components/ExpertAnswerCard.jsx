export default function ExpertAnswerCard({ expertName, answer, categoryColor }) {
  // Try to parse out the bold parts or markdown for better rendering
  return (
    <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-sm hover:shadow-md transition-shadow">
      <div className="bg-slate-50 px-4 py-3 border-b border-slate-200 flex items-center gap-2">
        <div className={`w-3 h-3 rounded-full ${categoryColor || "bg-slate-400"}`}></div>
        <h3 className="font-semibold text-slate-800">{expertName}</h3>
      </div>
      <div className="p-4 text-slate-600 text-sm leading-relaxed whitespace-pre-wrap max-h-64 overflow-y-auto">
        {answer}
      </div>
    </div>
  );
}
