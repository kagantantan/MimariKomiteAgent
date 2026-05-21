import React, { useState, useRef } from 'react';
import axios from 'axios';
import { UploadCloud, CheckCircle2, ArrowRight, Settings, MessageSquare, Send } from 'lucide-react';
import MermaidDiagram from '../components/MermaidDiagram';

export default function DiagramPage() {
  const [step, setStep] = useState(1);
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [asIsAnalysis, setAsIsAnalysis] = useState(null);
  const [validationQuestions, setValidationQuestions] = useState([]);
  const [userAnswer, setUserAnswer] = useState('');
  const [toBeRecommendation, setToBeRecommendation] = useState('');
  const [mermaidCode, setMermaidCode] = useState('');
  const [loading, setLoading] = useState(false);
  const fileInputRef = useRef(null);

  const handleFileChange = (e) => {
    const selected = e.target.files[0];
    if (selected) {
      setFile(selected);
      setPreview(URL.createObjectURL(selected));
    }
  };

  const startAnalysis = async () => {
    if (!file) return;
    setLoading(true);
    setStep(2);
    const formData = new FormData();
    formData.append('file', file);
    try {
      const res = await axios.post('/api/diagram/analyze', formData);
      setAsIsAnalysis(res.data);
      const vRes = await axios.post('/api/diagram/validate', {
        analysis: JSON.stringify(res.data),
        userAnswer: ''
      });
      setValidationQuestions(vRes.data.questions || []);
    } catch (err) {
      console.error(err);
      setAsIsAnalysis({ analysis: 'Analiz sırasında hata oluştu.', components: [], issues: [] });
    } finally {
      setLoading(false);
    }
  };

  const submitAnswer = async () => {
    if (!userAnswer.trim()) return;
    setLoading(true);
    try {
      const res = await axios.post('/api/diagram/validate', {
        analysis: `Mimari Analiz Özeti: ${asIsAnalysis.analysis}
        
Tespit Edilen Bileşenler: ${asIsAnalysis.components.join(', ')}

Sorunlar: ${asIsAnalysis.issues.join(', ')}`,
        userAnswer
      });
      setValidationQuestions(res.data.questions || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const generateToBe = async () => {
    setStep(3);
    setLoading(true);
    try {
      const res = await axios.post('/api/diagram/generate', {
        asIs: `Mimari Analiz Özeti: ${asIsAnalysis.analysis}
        
Tespit Edilen Bileşenler: ${asIsAnalysis.components.join(', ')}

Sorunlar: ${asIsAnalysis.issues.join(', ')}`,
        confirmed: true
      });
      setToBeRecommendation(res.data.toBe);
      setMermaidCode(res.data.plantUml);
    } catch (err) {
      console.error(err);
      setToBeRecommendation('To-Be üretilirken hata oluştu.');
    } finally {
      setLoading(false);
    }
  };

  const copyCode = () => {
    const el = document.createElement('textarea');
    el.value = mermaidCode;
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);
    alert('Kopyalandı!');
  };

  const downloadPng = () => {
    const svg = document.querySelector('#mermaid-diagram svg');
    if (!svg) return alert('Diagram henüz yüklenmedi.');
    const canvas = document.createElement('canvas');
    const svgData = new XMLSerializer().serializeToString(svg);
    const img = new Image();
    const blob = new Blob([svgData], { type: 'image/svg+xml;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    img.onload = () => {
      canvas.width = img.width;
      canvas.height = img.height;
      canvas.getContext('2d').drawImage(img, 0, 0);
      const a = document.createElement('a');
      a.download = 'to-be-diagram.png';
      a.href = canvas.toDataURL('image/png');
      a.click();
      URL.revokeObjectURL(url);
    };
    img.src = url;
  };

  const resetToStep1 = () => {
    setStep(1);
    setFile(null);
    setPreview(null);
    setAsIsAnalysis(null);
    setValidationQuestions([]);
    setUserAnswer('');
    setToBeRecommendation('');
    setMermaidCode('');
  };

  return (
    <div className="flex-1 overflow-y-auto p-8 bg-slate-50">
      <div className="max-w-6xl mx-auto">
        <div className="mb-8">
          <h2 className="text-2xl font-bold text-slate-800">Mimari Diagram Analizi</h2>
          <p className="text-slate-500">Diyagramınızı yükleyin, uzmanlar As-Is'i doğrulasın, To-Be önerisi alsın.</p>
        </div>

        <div className="flex items-center justify-between mb-12 max-w-2xl mx-auto relative">
          <div className="absolute left-0 right-0 top-4 h-0.5 bg-slate-200 -z-10"></div>
          {[['1','Yükle'], ['2','As-Is Doğrula'], ['3','To-Be Üret']].map(([n, label]) => (
            <div key={n} className="flex flex-col items-center gap-2 bg-slate-50 px-4">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center font-bold text-sm ${step >= parseInt(n) ? 'bg-blue-600 text-white' : 'bg-slate-200 text-slate-500'}`}>{n}</div>
              <span className="text-xs font-medium text-slate-600">{label}</span>
            </div>
          ))}
        </div>

        {step === 1 && (
          <div className="bg-white border-2 border-dashed border-slate-300 rounded-2xl p-12 text-center max-w-2xl mx-auto">
            {preview ? (
              <div className="flex flex-col items-center">
                <img src={preview} alt="Preview" className="max-h-64 rounded-lg shadow-sm border border-slate-200 mb-6" />
                <button onClick={startAnalysis} className="px-6 py-3 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 flex items-center gap-2">
                  Analizi Başlat <ArrowRight size={18} />
                </button>
              </div>
            ) : (
              <div className="flex flex-col items-center cursor-pointer" onClick={() => fileInputRef.current.click()}>
                <div className="w-16 h-16 bg-blue-50 text-blue-500 rounded-full flex items-center justify-center mb-4">
                  <UploadCloud size={32} />
                </div>
                <h3 className="text-lg font-semibold text-slate-800 mb-2">Diyagramı sürükle veya seç</h3>
                <p className="text-slate-500 mb-6">PNG, JPG desteklenir</p>
                <input type="file" ref={fileInputRef} onChange={handleFileChange} className="hidden" accept="image/*" />
                <button className="px-5 py-2 bg-white border border-slate-300 text-slate-700 rounded-lg hover:bg-slate-50 font-medium">Dosya Seç</button>
              </div>
            )}
          </div>
        )}

        {step === 2 && (
          <div>
            <button onClick={resetToStep1} className="text-sm text-slate-500 hover:text-slate-700 flex items-center gap-1 mb-4">
              ← Yeni diagram yükle
            </button>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                <h3 className="font-semibold text-slate-800 mb-4">Yüklenen Diyagram</h3>
                <img src={preview} alt="As Is" className="max-w-full rounded-lg border border-slate-200" />
              </div>

              <div className="flex flex-col gap-4">
                <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                  <h3 className="font-semibold text-slate-800 mb-3 flex items-center gap-2">
                    <Settings className="text-blue-600" size={18} />
                    Claude Vision Analizi
                  </h3>
                  {loading && !asIsAnalysis ? (
                    <div className="flex items-center gap-3 text-slate-500 text-sm">
                      <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600"></div>
                      Diyagram analiz ediliyor...
                    </div>
                  ) : asIsAnalysis ? (
                    <div>
                      {asIsAnalysis.analysis.includes('[Uzman Degerlendirmesi]') ? (
                        <div>
                          <p className="text-sm text-slate-700 mb-3">
                            {asIsAnalysis.analysis.split('[Uzman Degerlendirmesi]')[0]}
                          </p>
                          <div className="bg-purple-50 border-l-4 border-purple-400 p-3 rounded-lg mt-2">
                            <p className="text-xs font-semibold text-purple-700 mb-1">🎓 15 Uzman Sentezi</p>
                            <p className="text-xs text-purple-800 leading-relaxed">
                              {asIsAnalysis.analysis.split('[Uzman Degerlendirmesi]:')[1]}
                            </p>
                          </div>
                        </div>
                      ) : (
                        <p className="text-sm text-slate-700 mb-3">{asIsAnalysis.analysis}</p>
                      )}
                      <div className="mb-2">
                        <span className="text-xs font-semibold text-slate-500 uppercase">Bileşenler</span>
                        <ul className="mt-1 space-y-1">
                          {asIsAnalysis.components.map((c, i) => (
                            <li key={i} className="text-xs text-slate-600 flex items-start gap-1"><span className="text-blue-400 mt-0.5">▸</span>{c}</li>
                          ))}
                        </ul>
                      </div>
                      <div>
                        <span className="text-xs font-semibold text-slate-500 uppercase">Sorunlar</span>
                        <ul className="mt-1 space-y-1">
                          {asIsAnalysis.issues.map((issue, i) => (
                            <li key={i} className="text-xs text-amber-700 flex items-start gap-1"><span className="mt-0.5">⚠</span>{issue}</li>
                          ))}
                        </ul>
                      </div>
                    </div>
                  ) : null}
                </div>

                {validationQuestions.length > 0 && (
                  <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                    <h3 className="font-semibold text-slate-800 mb-3 flex items-center gap-2">
                      <MessageSquare className="text-purple-600" size={18} />
                      Uzman Doğrulama Soruları
                    </h3>
                    <ul className="space-y-2 mb-4">
                      {validationQuestions.map((q, i) => (
                        <li key={i} className="text-sm text-slate-700 bg-purple-50 rounded-lg p-3">{q}</li>
                      ))}
                    </ul>
                    <div className="flex gap-2">
                      <input
                        type="text"
                        value={userAnswer}
                        onChange={e => setUserAnswer(e.target.value)}
                        onKeyDown={e => e.key === 'Enter' && submitAnswer()}
                        placeholder="Soruları yanıtlayın..."
                        className="flex-1 text-sm border border-slate-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-purple-300"
                      />
                      <button onClick={submitAnswer} disabled={loading} className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700">
                        <Send size={16} />
                      </button>
                    </div>
                  </div>
                )}

                {asIsAnalysis && (
                  <div className="bg-blue-50 border border-blue-100 p-5 rounded-xl flex items-center justify-between">
                    <div>
                      <h4 className="font-semibold text-slate-800 text-sm">As-Is onaylandı mı?</h4>
                      <p className="text-xs text-slate-500 mt-1">Onaylayarak To-Be mimarisini üretin.</p>
                    </div>
                    <button onClick={generateToBe} className="px-5 py-2 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 flex items-center gap-2 text-sm">
                      To-Be Üret <ArrowRight size={16} />
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {step === 3 && (
          <div>
            <button onClick={() => setStep(2)} className="text-sm text-slate-500 hover:text-slate-700 flex items-center gap-1 mb-4">
              ← As-Is Analizine Dön
            </button>
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              <div className="lg:col-span-1 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                <h3 className="font-semibold text-slate-800 mb-4 flex items-center gap-2">
                  <CheckCircle2 className="text-green-600" size={20} />
                  To-Be Öneriler
                </h3>
                {loading ? (
                  <div className="flex items-center gap-3 text-slate-500 text-sm">
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-green-600"></div>
                    To-Be mimarisi üretiliyor...
                  </div>
                ) : (
                  <p className="text-sm text-slate-700 leading-relaxed">{toBeRecommendation}</p>
                )}
              </div>

              <div className="lg:col-span-2 bg-slate-800 p-6 rounded-xl shadow-lg">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="font-semibold text-slate-100">To-Be Sequence Diagram</h3>
                  {mermaidCode && (
                    <div className="flex gap-2">
                      <button onClick={copyCode} className="text-xs px-3 py-1 bg-slate-700 text-slate-300 rounded hover:bg-slate-600">
                        Kodu Kopyala
                      </button>
                      <button onClick={downloadPng} className="text-xs px-3 py-1 bg-green-700 text-green-200 rounded hover:bg-green-600">
                        PNG İndir
                      </button>
                    </div>
                  )}
                </div>
                {loading ? (
                  <p className="text-slate-500 text-sm">Üretiliyor...</p>
                ) : (
                  <MermaidDiagram code={mermaidCode} />
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
