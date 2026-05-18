import React, { useState } from 'react';
import axios from 'axios';
import { Send, Loader2 } from 'lucide-react';
import SynthesisCard from '../components/SynthesisCard';
import ExpertAnswerCard from '../components/ExpertAnswerCard';

export default function ConsultPage() {
  const [question, setQuestion] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  const expertColors = {
    "Eric Evans (DDD)": "bg-purple-500",
    "Vaughn Vernon (DDD)": "bg-purple-500",
    "Vlad Khononov (Modern DDD)": "bg-purple-500",
    "Sam Newman (Microservices)": "bg-teal-500",
    "Chris Richardson (Microservices Patterns)": "bg-orange-500",
    "Michael Feathers (Legacy Code)": "bg-red-500",
    "Uncle Bob (Clean Architecture)": "bg-green-500",
    "Kent Beck (TDD)": "bg-green-500",
    "Martin Fowler (Refactoring & Patterns)": "bg-orange-500",
    "Pramod Sadalage (Data Architecture)": "bg-blue-500",
    "Martin Kleppmann (Distributed Data)": "bg-blue-500",
    "Alberto Brandolini (Event Storming)": "bg-purple-500",
    "Nick Tune (Strategic DDD)": "bg-purple-500"
  };

  const handleAsk = async (e) => {
    e.preventDefault();
    if (!question.trim()) return;

    setLoading(true);
    setResult(null);
    
    const submittedQuestion = question;
    setQuestion(''); // Soruyu input'tan anında temizle

    try {
      // Points to Spring Boot backend
      const response = await axios.post('/api/consult', { question: submittedQuestion });
      setResult(response.data);
    } catch (error) {
      console.error("Failed to fetch consultation", error);
      alert("Consultation failed. Check console or make sure backend is running on 8080.");
      setQuestion(submittedQuestion); // Hata olursa soruyu geri getir
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex-1 flex flex-col h-screen overflow-hidden bg-slate-50">
      <div className="flex-1 overflow-y-auto p-8">
        <div className="max-w-5xl mx-auto">
          {!result && !loading && (
            <div className="flex flex-col items-center justify-center h-[60vh] text-center">
              <div className="w-16 h-16 bg-blue-100 rounded-2xl flex items-center justify-center mb-6">
                <span className="text-2xl">🏛️</span>
              </div>
              <h2 className="text-2xl font-bold text-slate-800 mb-2">Ask the Architecture Committee</h2>
              <p className="text-slate-500 max-w-lg">
                Describe your architectural problem, challenge, or question. The AI Orchestrator will route it to the best experts and synthesize their answers.
              </p>
            </div>
          )}

          {loading && (
            <div className="flex flex-col items-center justify-center h-[60vh]">
              <Loader2 className="animate-spin text-blue-600 mb-4" size={40} />
              <p className="text-slate-600 font-medium animate-pulse">The Committee is convening...</p>
              <p className="text-sm text-slate-400 mt-2">Consulting with up to 15 experts in parallel.</p>
            </div>
          )}

          {result && (
            <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
              <div className="mb-8 p-6 bg-white rounded-xl border border-slate-200 shadow-sm">
                <h3 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-2">Your Question</h3>
                <p className="text-lg text-slate-800">{result.question}</p>
              </div>

              <SynthesisCard synthesis={result.synthesis} />

              <h3 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-4 mt-8">Individual Expert Opinions</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pb-20">
                {Object.entries(result.expertAnswers || {}).map(([expert, answer]) => (
                  <ExpertAnswerCard 
                    key={expert} 
                    expertName={expert} 
                    answer={answer} 
                    categoryColor={expertColors[expert] || "bg-slate-500"} 
                  />
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="p-6 bg-white border-t border-slate-200">
        <div className="max-w-4xl mx-auto">
          <form onSubmit={handleAsk} className="relative shadow-sm rounded-xl overflow-hidden border border-slate-300 focus-within:border-blue-500 focus-within:ring-1 focus-within:ring-blue-500 transition-shadow">
            <textarea
              className="w-full resize-none p-4 pr-16 bg-white outline-none text-slate-800 placeholder-slate-400"
              placeholder="E.g., How should I split my 15-year old Oracle DB monolith?"
              rows={3}
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  handleAsk(e);
                }
              }}
            />
            <button 
              type="submit" 
              disabled={loading || !question.trim()}
              className="absolute right-3 bottom-3 p-2 bg-blue-600 hover:bg-blue-700 disabled:bg-slate-300 text-white rounded-lg transition-colors flex items-center justify-center"
            >
              <Send size={18} />
            </button>
          </form>
          <p className="text-xs text-center text-slate-400 mt-2">Shift + Enter for new line</p>
        </div>
      </div>
    </div>
  );
}
