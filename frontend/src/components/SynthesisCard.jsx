import React from 'react';
import { Sparkles, AlertCircle, ArrowRightCircle } from "lucide-react";

export default function SynthesisCard({ synthesis }) {
  // Very basic parser for the structured output from OrchestratorService
  // Expects: 1. **Consensus**, 2. **Key Disagreements**, 3. **Recommended Action**, 4. **Primary Expert**
  
  if (!synthesis) return null;

  return (
    <div className="bg-gradient-to-br from-blue-50 to-indigo-50 border border-blue-100 rounded-xl p-6 shadow-sm mb-8">
      <div className="flex items-center gap-2 mb-4">
        <Sparkles className="text-blue-600" size={24} />
        <h2 className="text-xl font-bold text-slate-800">Committee Synthesis</h2>
      </div>
      
      <div className="prose prose-sm max-w-none text-slate-700 whitespace-pre-wrap">
        {synthesis}
      </div>
    </div>
  );
}
