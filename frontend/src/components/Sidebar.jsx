import { MessageSquare, Image as ImageIcon, Database, UserCircle2 } from "lucide-react";
import { Link, useLocation } from "react-router-dom";

export default function Sidebar() {
  const location = useLocation();

  const experts = [
    { name: "Eric Evans", category: "DDD", color: "bg-purple-500" },
    { name: "Vaughn Vernon", category: "DDD", color: "bg-purple-500" },
    { name: "Vlad Khononov", category: "DDD", color: "bg-purple-500" },
    { name: "Mathias Verraes", category: "DDD", color: "bg-purple-500" },
    { name: "Alberto Brandolini", category: "DDD", color: "bg-purple-500" },
    { name: "Nick Tune", category: "DDD", color: "bg-purple-500" },
    { name: "Sam Newman", category: "Microservices", color: "bg-teal-500" },
    { name: "Chris Richardson", category: "Patterns", color: "bg-orange-500" },
    { name: "Martin Fowler", category: "Patterns", color: "bg-orange-500" },
    { name: "Neal Ford & Mark Richards", category: "Architecture", color: "bg-orange-500" },
    { name: "Michael Feathers", category: "Legacy", color: "bg-red-500" },
    { name: "Martin Kleppmann", category: "Data", color: "bg-blue-500" },
    { name: "Pramod Sadalage", category: "Data", color: "bg-blue-500" },
    { name: "Kent Beck", category: "XP", color: "bg-green-500" },
    { name: "Uncle Bob", category: "Clean Code", color: "bg-green-500" },
  ];

  const menu = [
    { name: "Consultation", path: "/consult", icon: <MessageSquare size={18} /> },
    { name: "Diagram Analysis", path: "/diagram", icon: <ImageIcon size={18} /> },
    { name: "Knowledge Base", path: "/knowledge", icon: <Database size={18} /> },
  ];

  return (
    <div className="w-64 bg-white border-r border-slate-200 h-screen flex flex-col">
      <div className="p-6 border-b border-slate-200 flex items-center gap-3">
        <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center text-white font-bold">
          MK
        </div>
        <h1 className="font-bold text-lg text-slate-800">Mimari Komite</h1>
      </div>

      <div className="p-4 flex-1 overflow-y-auto">
        <div className="mb-6">
          <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3 px-2">Navigation</p>
          <ul className="space-y-1">
            {menu.map((item) => {
              const active = location.pathname === item.path || (location.pathname === "/" && item.path === "/consult");
              return (
                <li key={item.name}>
                  <Link
                    to={item.path}
                    className={`flex items-center gap-3 px-3 py-2 rounded-md transition-colors ${
                      active ? "bg-blue-50 text-blue-600 font-medium" : "text-slate-600 hover:bg-slate-50"
                    }`}
                  >
                    {item.icon}
                    {item.name}
                  </Link>
                </li>
              );
            })}
          </ul>
        </div>

        <div>
          <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3 px-2">Committee Members</p>
          <ul className="space-y-2">
            {experts.map((exp) => (
              <li key={exp.name} className="flex items-center gap-3 px-2 py-1.5 hover:bg-slate-50 rounded-md cursor-pointer transition-colors group">
                <UserCircle2 className="text-slate-400 group-hover:text-slate-600" size={18} />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-slate-700 truncate">{exp.name}</p>
                  <p className="text-xs text-slate-500 truncate">{exp.category}</p>
                </div>
                <div className={`w-2 h-2 rounded-full ${exp.color}`}></div>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}
