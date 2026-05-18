import React, { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import { UploadCloud, FileText, BookOpen, Check, Loader } from 'lucide-react';

const EXPERTS = [
  { id: 'eric-evans', name: 'Eric Evans', category: 'DDD', color: '#7F77DD' },
  { id: 'vaughn-vernon', name: 'Vaughn Vernon', category: 'DDD', color: '#7F77DD' },
  { id: 'vlad-khononov', name: 'Vlad Khononov', category: 'DDD', color: '#7F77DD' },
  { id: 'mathias-verraes', name: 'Mathias Verraes', category: 'DDD', color: '#7F77DD' },
  { id: 'alberto-brandolini', name: 'Alberto Brandolini', category: 'DDD', color: '#7F77DD' },
  { id: 'nick-tune', name: 'Nick Tune', category: 'DDD', color: '#7F77DD' },
  { id: 'sam-newman', name: 'Sam Newman', category: 'Microservices', color: '#1D9E75' },
  { id: 'chris-richardson', name: 'Chris Richardson', category: 'Microservices', color: '#1D9E75' },
  { id: 'martin-fowler', name: 'Martin Fowler', category: 'Patterns', color: '#D85A30' },
  { id: 'michael-feathers', name: 'Michael Feathers', category: 'Legacy', color: '#E24B4A' },
  { id: 'martin-kleppmann', name: 'Martin Kleppmann', category: 'Data', color: '#378ADD' },
  { id: 'pramod-sadalage', name: 'Pramod Sadalage', category: 'Data', color: '#378ADD' },
  { id: 'neal-ford-mark-richards', name: 'Ford & Richards', category: 'Architecture', color: '#BA7517' },
  { id: 'kent-beck', name: 'Kent Beck', category: 'XP', color: '#639922' },
  { id: 'uncle-bob', name: 'Uncle Bob', category: 'Clean Code', color: '#639922' },
];

export default function KnowledgePage() {
  const [selectedExpert, setSelectedExpert] = useState(null);
  const [mode, setMode] = useState('pdf');
  const [sourceName, setSourceName] = useState('');
  const [text, setText] = useState('');
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  const handleUpload = async () => {
    if (!selectedExpert || !sourceName.trim()) {
      setError('Lütfen uzman ve kaynak adı seçin.');
      return;
    }
    if (mode === 'pdf' && !file) {
      setError('Lütfen PDF dosyası seçin.');
      return;
    }
    if (mode === 'text' && !text.trim()) {
      setError('Lütfen metin girin.');
      return;
    }

    setLoading(true);
    setResult(null);
    setError(null);

    try {
      let res;
      if (mode === 'pdf') {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('expert', selectedExpert);
        formData.append('sourceName', sourceName);
        res = await axios.post('/api/knowledge/ingest-pdf', formData);
      } else {
        res = await axios.post('/api/knowledge/ingest-text', {
          expert: selectedExpert,
          text,
          sourceName
        });
      }
      setResult(res.data);
      setFile(null);
      setText('');
      setSourceName('');
    } catch (err) {
      setError('Yükleme sırasında hata oluştu: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex-1 overflow-y-auto p-8 bg-slate-50">
      <div className="max-w-6xl mx-auto">
        <div className="mb-8">
          <h2 className="text-2xl font-bold text-slate-800">Bilgi Tabanı</h2>
          <p className="text-slate-500">Uzmanlara PDF veya metin yükleyerek bilgi tabanını genişletin.</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

          {/* Sol: Uzman listesi */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
            <h3 className="font-semibold text-slate-800 mb-4 flex items-center gap-2">
              <BookOpen size={18} className="text-blue-600" />
              Uzman Seç
            </h3>
            <div className="space-y-1">
              {EXPERTS.map(expert => (
                <button
                  key={expert.id}
                  onClick={() => setSelectedExpert(expert.id)}
                  className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-left transition-colors ${
                    selectedExpert === expert.id
                      ? 'bg-blue-50 border border-blue-200'
                      : 'hover:bg-slate-50'
                  }`}
                >
                  <div className="w-2.5 h-2.5 rounded-full flex-shrink-0" style={{ background: expert.color }}></div>
                  <div className="flex-1 min-w-0">
                    <div className="text-sm font-medium text-slate-800 truncate">{expert.name}</div>
                    <div className="text-xs text-slate-400">{expert.category}</div>
                  </div>
                  {selectedExpert === expert.id && (
                    <Check size={14} className="text-blue-600 flex-shrink-0" />
                  )}
                </button>
              ))}
            </div>
          </div>

          {/* Sağ: Yükleme alanı */}
          <div className="lg:col-span-2 flex flex-col gap-6">

            {/* Kaynak adı */}
            <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
              <label className="block text-sm font-medium text-slate-700 mb-2">Kaynak Adı</label>
              <input
                type="text"
                value={sourceName}
                onChange={e => setSourceName(e.target.value)}
                placeholder="Örn: DDD Blue Book, Martin Fowler Blog Post..."
                className="w-full text-sm border border-slate-300 rounded-lg px-3 py-2.5 focus:outline-none focus:ring-2 focus:ring-blue-300"
              />
            </div>

            {/* Mod seçimi */}
            <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
              <div className="flex gap-3 mb-6">
                <button
                  onClick={() => setMode('pdf')}
                  className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium border transition-colors ${
                    mode === 'pdf'
                      ? 'bg-blue-600 text-white border-blue-600'
                      : 'bg-white text-slate-600 border-slate-300 hover:bg-slate-50'
                  }`}
                >
                  <UploadCloud size={16} />
                  PDF Yükle
                </button>
                <button
                  onClick={() => setMode('text')}
                  className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium border transition-colors ${
                    mode === 'text'
                      ? 'bg-blue-600 text-white border-blue-600'
                      : 'bg-white text-slate-600 border-slate-300 hover:bg-slate-50'
                  }`}
                >
                  <FileText size={16} />
                  Metin Yapıştır
                </button>
              </div>

              {mode === 'pdf' ? (
                <div>
                  <div
                    onClick={() => fileInputRef.current.click()}
                    className="border-2 border-dashed border-slate-300 rounded-xl p-10 text-center cursor-pointer hover:border-blue-400 hover:bg-blue-50 transition-colors"
                  >
                    {file ? (
                      <div className="flex flex-col items-center gap-2">
                        <div className="w-12 h-12 bg-green-100 text-green-600 rounded-full flex items-center justify-center">
                          <Check size={24} />
                        </div>
                        <p className="text-sm font-medium text-slate-800">{file.name}</p>
                        <p className="text-xs text-slate-400">{(file.size / 1024 / 1024).toFixed(2)} MB</p>
                        <p className="text-xs text-blue-500">Değiştirmek için tıkla</p>
                      </div>
                    ) : (
                      <div className="flex flex-col items-center gap-2">
                        <div className="w-12 h-12 bg-blue-50 text-blue-500 rounded-full flex items-center justify-center">
                          <UploadCloud size={24} />
                        </div>
                        <p className="text-sm font-medium text-slate-700">PDF dosyasını sürükle veya tıkla</p>
                        <p className="text-xs text-slate-400">Maks 50MB</p>
                      </div>
                    )}
                  </div>
                  <input
                    type="file"
                    ref={fileInputRef}
                    onChange={e => setFile(e.target.files[0])}
                    className="hidden"
                    accept=".pdf"
                  />
                </div>
              ) : (
                <textarea
                  value={text}
                  onChange={e => setText(e.target.value)}
                  placeholder="Makale metnini buraya yapıştırın..."
                  className="w-full h-64 text-sm border border-slate-300 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-300 resize-none font-mono"
                />
              )}
            </div>

            {/* Hata / Sonuç */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-xl p-4 text-sm text-red-700">
                {error}
              </div>
            )}

            {result && (
              <div className="bg-green-50 border border-green-200 rounded-xl p-4 flex items-center gap-3">
                <Check size={20} className="text-green-600 flex-shrink-0" />
                <div>
                  <p className="text-sm font-medium text-green-800">Başarıyla yüklendi!</p>
                  <p className="text-xs text-green-600 mt-0.5">
                    {result.chunksLoaded} chunk · {result.expert} · {result.source}
                  </p>
                </div>
              </div>
            )}

            {/* Yükle butonu */}
            <button
              onClick={handleUpload}
              disabled={loading || !selectedExpert}
              className="w-full py-3 bg-blue-600 text-white font-medium rounded-xl hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {loading ? (
                <>
                  <Loader size={18} className="animate-spin" />
                  Yükleniyor...
                </>
              ) : (
                <>
                  <UploadCloud size={18} />
                  {selectedExpert
                    ? `${EXPERTS.find(e => e.id === selectedExpert)?.name} için Yükle`
                    : 'Önce Uzman Seçin'}
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
