export function Features() {
  const features = [
    { icon: '🚨', title: 'Báo động nhanh', desc: 'Gửi yêu cầu cứu hộ chỉ với vài thao tác, có thể đính kèm ảnh và vị trí chính xác.' },
    { icon: '📍', title: 'Định vị thông minh', desc: 'Chọn vị trí trên bản đồ, đội cứu hộ sẽ đến nhanh nhất.' },
    { icon: '📊', title: 'Theo dõi trực tiếp', desc: 'Cập nhật trạng thái yêu cầu real-time, biết khi nào đội cứu hộ đến.' },
  ];

  return (
    <section className="grid md:grid-cols-3 gap-8">
      {features.map((f, i) => (
        <div key={i} className="text-center p-6 border rounded-lg">
          <div className="text-4xl mb-4">{f.icon}</div>
          <h3 className="text-xl font-bold mb-2">{f.title}</h3>
          <p>{f.desc}</p>
        </div>
      ))}
    </section>
  );
}