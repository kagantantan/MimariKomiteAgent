import { useEffect, useRef } from 'react';
import mermaid from 'mermaid';

mermaid.initialize({ startOnLoad: false, theme: 'default' });

export default function MermaidDiagram({ code }) {
  const ref = useRef(null);

  useEffect(() => {
    if (!code || !ref.current) return;
    const render = async () => {
      try {
        const id = 'mermaid-' + Date.now();
        const { svg } = await mermaid.render(id, code);
        ref.current.innerHTML = svg;
      } catch (e) {
        ref.current.innerHTML = '<pre style="color:red;font-size:12px;">' + e.message + '</pre>';
      }
    };
    render();
  }, [code]);

  return <div id="mermaid-diagram" ref={ref} className="w-full overflow-auto bg-white rounded-lg p-4" />;
}
