import PatientNav from "./PatientNav";

export default function MessageDoctorPage() {
  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
        <header className=" items-center justify-between mb-8 bg-[#b0372b] p-4  relative">
            <PatientNav activePage="Message Doctor" />
            </header>

        <div className="m-6 ">

      <h1 className="text-3xl font-bold mb-4">Message Doctor</h1>
      <p className="text-gray-700 mb-6">
        Send a secure message or attach your latest report for review by your physician.
      </p>

      <div className="bg-white border border-gray-300 rounded-lg p-6 max-w-xl shadow-sm">
        <form className="flex flex-col gap-4">
          <label className="font-medium">Subject</label>
          <input
            type="text"
            placeholder="E.g., Follow-up on last diagnosis"
            className="border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-[#b0372b]"
          />

          <label className="font-medium">Message</label>
          <textarea
            placeholder="Type your message..."
            rows="4"
            className="border border-gray-300 rounded-md p-2 resize-none focus:ring-2 focus:ring-[#b0372b]"
          ></textarea>

          <button
            type="submit"
            className="bg-[#b0372b] text-white rounded-md py-2 hover:bg-[#962b21] transition"
          >
            Send Message
          </button>
        </form>
      </div>
      </div>
    </div>
  );
}
